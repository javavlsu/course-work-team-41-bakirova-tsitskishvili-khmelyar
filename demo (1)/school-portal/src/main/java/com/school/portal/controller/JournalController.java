package com.school.portal.controller;

import com.school.portal.model.*;
import com.school.portal.model.dto.*;
import com.school.portal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/journal")
public class JournalController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchoolClassRepository classRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentClassRepository studentClassRepository;

    @Autowired
    private ClassSubjectTeacherRepository classSubjectTeacherRepository;

    @GetMapping("/index")
    public String index(
            @RequestParam(value = "classId", required = false) Integer classId,
            @RequestParam(value = "subjectId", required = false) Integer subjectId,
            @RequestParam(value = "weekStart", required = false) String weekStartStr,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        String role = currentUser.getRole().getRoleName();
        boolean isDirector = "DIRECTOR".equals(role);

        LocalDate today = LocalDate.now();
        LocalDate currentMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate weekStart = currentMonday;
        if (weekStartStr != null && !weekStartStr.isEmpty()) {
            try {
                weekStart = LocalDate.parse(weekStartStr);
            } catch (Exception e) {
                weekStart = currentMonday;
            }
        }

        // Генерация доступных недель
        LocalDate academicYearStart = LocalDate.of(
                today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1, 9, 1);
        LocalDate firstAcademicMonday = academicYearStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Map<String, String> availableWeeks = new LinkedHashMap<>();
        LocalDate week = firstAcademicMonday;
        int weekNumber = 1;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        while (!week.isAfter(currentMonday.plusWeeks(4))) {
            LocalDate weekEnd = week.plusDays(6);
            String weekLabel = weekNumber + " нед. (" + week.format(formatter) + " - " + weekEnd.format(formatter) + ")";
            availableWeeks.put(week.toString(), weekLabel);
            week = week.plusWeeks(1);
            weekNumber++;
        }

        // Доступные классы
        Map<Integer, String> availableClasses = new LinkedHashMap<>();
        // Доступные предметы
        Map<Integer, String> availableSubjects = new LinkedHashMap<>();

        if (isDirector) {
            // Для директора - все классы и предметы
            List<SchoolClass> allClasses = classRepository.findAll();
            for (SchoolClass cls : allClasses) {
                availableClasses.put(cls.getClassId(), cls.getClassName());
            }

            List<Subject> allSubjects = subjectRepository.findAll();
            for (Subject subj : allSubjects) {
                availableSubjects.put(subj.getSubjectId(), subj.getSubjectName());
            }
        } else {
            // Для учителя - только его классы и предметы
            List<SchoolClass> teacherClasses = classRepository.findClassesByTeacherId(currentUser.getUserId());
            for (SchoolClass cls : teacherClasses) {
                availableClasses.put(cls.getClassId(), cls.getClassName());
            }

            List<Subject> teacherSubjects = subjectRepository.findSubjectsByTeacherId(currentUser.getUserId());
            for (Subject subj : teacherSubjects) {
                availableSubjects.put(subj.getSubjectId(), subj.getSubjectName());
            }
        }

        // Если списки пустые, добавляем заглушки для демо
        if (availableClasses.isEmpty()) {
            availableClasses.put(1, "9 \"А\"");
            availableClasses.put(2, "9 \"Б\"");
            availableClasses.put(3, "10 \"А\"");
        }

        if (availableSubjects.isEmpty()) {
            availableSubjects.put(1, "Математика");
            availableSubjects.put(2, "Русский язык");
            availableSubjects.put(3, "Физика");
        }

        int selectedClassId = classId != null && availableClasses.containsKey(classId) ?
                classId : availableClasses.keySet().iterator().next();
        int selectedSubjectId = subjectId != null && availableSubjects.containsKey(subjectId) ?
                subjectId : availableSubjects.keySet().iterator().next();

        // Получаем уроки для выбранного класса и недели
        LocalDateTime startDateTime = weekStart.atStartOfDay();
        LocalDateTime endDateTime = weekStart.plusDays(7).atTime(23, 59, 59);

        List<Schedule> lessonsForWeek = scheduleRepository.findLessonsForClassBetween(
                selectedClassId, startDateTime, endDateTime);

        // Получаем учеников класса
        List<StudentClass> studentClasses = studentClassRepository.findBySchoolClassClassId(selectedClassId);
        List<User> students = studentClasses.stream()
                .map(StudentClass::getStudent)
                .sorted(Comparator.comparing(User::getLastName))
                .collect(Collectors.toList());

        List<JournalRow> rows = new ArrayList<>();

        for (User student : students) {
            JournalRow row = new JournalRow();
            row.setStudent(student);

            for (Schedule lesson : lessonsForWeek) {
                CellData cell = new CellData();

                // Проверяем оценку
                Optional<Grade> gradeOpt = gradeRepository.findByStudentUserIdAndLessonLessonId(
                        student.getUserId(), lesson.getLessonId());

                if (gradeOpt.isPresent()) {
                    Grade grade = gradeOpt.get();
                    if (grade.getGradeValue() != null) {
                        cell.setValue(String.valueOf(grade.getGradeValue()));
                        cell.setAttendance(false);
                    }

                    if (grade.getComment() != null && !grade.getComment().isEmpty()) {
                        cell.setHasComment(true);
                        cell.setComment(grade.getComment());
                    }
                } else {
                    // Если нет оценки, проверяем посещаемость
                    Optional<Attendance> attendanceOpt = attendanceRepository
                            .findByStudentUserIdAndLessonLessonId(student.getUserId(), lesson.getLessonId());

                    if (attendanceOpt.isPresent()) {
                        cell.setValue(attendanceOpt.get().getStatus());
                        cell.setAttendance(true);
                    }
                }

                row.getCells().put(lesson.getLessonId(), cell);
            }

            rows.add(row);
        }

        JournalViewModel viewModel = new JournalViewModel();
        viewModel.setSelectedClassId(selectedClassId);
        viewModel.setSelectedSubjectId(selectedSubjectId);
        viewModel.setWeekStart(weekStart);
        viewModel.setClasses(availableClasses);
        viewModel.setSubjects(availableSubjects);
        viewModel.setWeeks(availableWeeks);
        viewModel.setLessonsForWeek(lessonsForWeek);
        viewModel.setRows(rows);

        model.addAttribute("viewModel", viewModel);
        model.addAttribute("title", "Ведение журнала");
        model.addAttribute("activePage", "journal");
        model.addAttribute("content", "journal/index");

        return "layout";
    }

    @PostMapping("/save-grade")
    @ResponseBody
    public Map<String, Object> saveGrade(
            @RequestParam int studentId,
            @RequestParam int lessonId,
            @RequestParam(required = false) String gradeValue,
            @RequestParam(required = false) String attendanceStatus,
            @RequestParam(required = false) String comment) {

        Map<String, Object> response = new HashMap<>();

        try {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Ученик не найден"));
            Schedule lesson = scheduleRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Урок не найден"));

            // Обработка оценки
            if (gradeValue != null && !gradeValue.isEmpty()) {
                // Проверяем существующую оценку
                Optional<Grade> existingGrade = gradeRepository
                        .findByStudentUserIdAndLessonLessonId(studentId, lessonId);

                Grade grade;
                if (existingGrade.isPresent()) {
                    grade = existingGrade.get();
                } else {
                    grade = new Grade();
                    grade.setStudent(student);
                    grade.setLesson(lesson);
                    grade.setDate(LocalDateTime.now());
                }

                grade.setGradeValue(Integer.parseInt(gradeValue));
                grade.setComment(comment);
                gradeRepository.save(grade);
            }

            // Обработка посещаемости (в отдельной таблице Attendance)
            if (attendanceStatus != null && !attendanceStatus.isEmpty()) {
                Optional<Attendance> existingAttendance = attendanceRepository
                        .findByStudentUserIdAndLessonLessonId(studentId, lessonId);

                Attendance attendance;
                if (existingAttendance.isPresent()) {
                    attendance = existingAttendance.get();
                } else {
                    attendance = new Attendance();
                    attendance.setStudent(student);
                    attendance.setLesson(lesson);
                }
                attendance.setStatus(attendanceStatus);
                attendanceRepository.save(attendance);
            }

            response.put("success", true);
            response.put("message", "Данные успешно сохранены");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка сохранения: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/clear-grade")
    @ResponseBody
    public Map<String, Object> clearGrade(
            @RequestParam int studentId,
            @RequestParam int lessonId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Удаляем оценку если есть
            gradeRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId)
                    .ifPresent(grade -> gradeRepository.delete(grade));

            // Удаляем запись о посещаемости если есть
            attendanceRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId)
                    .ifPresent(attendance -> attendanceRepository.delete(attendance));

            response.put("success", true);
            response.put("message", "Данные успешно очищены");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка очистки: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/lesson-form")
    public String lessonForm(@RequestParam int lessonId, Model model) {
        Schedule lesson = scheduleRepository.findById(lessonId)
                .orElse(null);
        model.addAttribute("lesson", lesson);
        return "journal/_lesson_form_partial";
    }

    @PostMapping("/save-lesson")
    @ResponseBody
    public Map<String, Object> saveLesson(
            @RequestParam int lessonId,
            @RequestParam(required = false) String lessonTopic,
            @RequestParam(required = false) String homeworkText) {

        Map<String, Object> response = new HashMap<>();

        try {
            Schedule lesson = scheduleRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Урок не найден"));

            lesson.setLessonTopic(lessonTopic);
            lesson.setHomeworkText(homeworkText);
            scheduleRepository.save(lesson);

            response.put("success", true);
            response.put("message", "Данные урока успешно сохранены");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка сохранения: " + e.getMessage());
        }

        return response;
    }
}