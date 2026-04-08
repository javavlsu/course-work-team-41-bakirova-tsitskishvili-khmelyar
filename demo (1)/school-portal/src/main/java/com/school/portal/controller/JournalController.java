package com.school.portal.controller;

import com.school.portal.model.*;
import com.school.portal.model.dto.*;
import com.school.portal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
    private RemarkRepository remarkRepository;

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

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
        boolean isDirectorOrAdmin = "DIRECTOR".equals(role) || "ADMIN".equals(role);

        LocalDate today = LocalDate.now();
        LocalDate currentMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Определяем выбранную неделю
        LocalDate weekStart;
        if (weekStartStr != null && !weekStartStr.isEmpty()) {
            try {
                weekStart = LocalDate.parse(weekStartStr);
            } catch (Exception e) {
                weekStart = currentMonday;
            }
        } else {
            weekStart = currentMonday;
        }

        // Генерация доступных недель
        Map<String, String> availableWeeks = generateAvailableWeeks();

        // Получение доступных классов и предметов
        Map<Integer, String> availableClasses = new LinkedHashMap<>();
        Map<Integer, String> availableSubjects = new LinkedHashMap<>();

        if (isDirectorOrAdmin) {
            // Директор и админ видят все классы и предметы
            classRepository.findAll().forEach(cls -> availableClasses.put(cls.getClassId(), cls.getClassName()));
            subjectRepository.findAll().forEach(subj -> availableSubjects.put(subj.getSubjectId(), subj.getSubjectName()));
        } else if ("TEACHER".equals(role)) {
            // Учитель видит классы, где он ведет уроки (через ClassSubjectTeacher)
            List<SchoolClass> teacherClasses = classRepository.findClassesByTeacherId(currentUser.getUserId());
            for (SchoolClass cls : teacherClasses) {
                availableClasses.put(cls.getClassId(), cls.getClassName());
            }

            // Учитель видит только предметы, которые он ведет
            List<Subject> teacherSubjects = subjectRepository.findSubjectsByTeacherId(currentUser.getUserId());
            for (Subject subj : teacherSubjects) {
                availableSubjects.put(subj.getSubjectId(), subj.getSubjectName());
            }
        }

        if (availableClasses.isEmpty()) {
            model.addAttribute("errorMessage", "У вас нет назначенных классов для ведения уроков");
            model.addAttribute("title", "Ведение журнала");
            model.addAttribute("activePage", "journal");
            model.addAttribute("content", "journal/index");
            return "layout";
        }

        if (availableSubjects.isEmpty()) {
            model.addAttribute("errorMessage", "У вас нет назначенных предметов");
            model.addAttribute("title", "Ведение журнала");
            model.addAttribute("activePage", "journal");
            model.addAttribute("content", "journal/index");
            return "layout";
        }

        int selectedClassId = (classId != null && availableClasses.containsKey(classId))
                ? classId : availableClasses.keySet().iterator().next();
        int selectedSubjectId = (subjectId != null && availableSubjects.containsKey(subjectId))
                ? subjectId : availableSubjects.keySet().iterator().next();

        // Получаем расписание на неделю
        LocalDateTime startDateTime = weekStart.atStartOfDay();
        LocalDateTime endDateTime = weekStart.plusDays(7).atTime(23, 59, 59);

        List<Schedule> allLessonsForWeek = scheduleRepository.findLessonsForClassBetween(
                selectedClassId, startDateTime, endDateTime);

        // Фильтруем уроки по выбранному предмету
        List<Schedule> lessonsForWeek = allLessonsForWeek.stream()
                .filter(lesson -> lesson.getSubject() != null && lesson.getSubject().getSubjectId() == selectedSubjectId)
                .collect(Collectors.toList());

        List<User> students = studentClassRepository.findStudentsByClassId(selectedClassId);
        students.sort(Comparator.comparing(User::getLastName));

        List<JournalRow> rows = new ArrayList<>();
        for (User student : students) {
            JournalRow row = new JournalRow();
            row.setStudent(student);

            for (Schedule lesson : lessonsForWeek) {
                CellData cell = new CellData();

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
                }

                if (cell.getValue() == null) {
                    Optional<Attendance> attendanceOpt = attendanceRepository
                            .findByStudentUserIdAndLessonLessonId(student.getUserId(), lesson.getLessonId());
                    if (attendanceOpt.isPresent()) {
                        cell.setValue(attendanceOpt.get().getStatus());
                        cell.setAttendance(true);
                    }
                }

                Optional<Remark> remarkOpt = remarkRepository
                        .findByStudentUserIdAndLessonLessonId(student.getUserId(), lesson.getLessonId());
                if (remarkOpt.isPresent() && remarkOpt.get().getText() != null && !remarkOpt.get().getText().isEmpty()) {
                    cell.setHasRemark(true);
                    cell.setRemark(remarkOpt.get().getText());
                }

                row.getCells().put(lesson.getLessonId(), cell);
            }
            rows.add(row);
        }

        // Получаем предыдущую и следующую недели для навигации
        LocalDate prevWeekStart = weekStart.minusWeeks(1);
        LocalDate nextWeekStart = weekStart.plusWeeks(1);
        boolean nextWeekAvailable = !nextWeekStart.isAfter(currentMonday);

        JournalViewModel viewModel = new JournalViewModel();
        viewModel.setSelectedClassId(selectedClassId);
        viewModel.setSelectedSubjectId(selectedSubjectId);
        viewModel.setWeekStart(weekStart);
        viewModel.setPrevWeekStart(prevWeekStart);
        viewModel.setNextWeekStart(nextWeekStart);
        viewModel.setNextWeekAvailable(nextWeekAvailable);
        viewModel.setClasses(availableClasses);
        viewModel.setSubjects(availableSubjects);
        viewModel.setWeeks(availableWeeks);
        viewModel.setLessonsForWeek(lessonsForWeek);
        viewModel.setRows(rows);
        viewModel.setSelectedClassName(availableClasses.get(selectedClassId));
        viewModel.setSelectedSubjectName(availableSubjects.get(selectedSubjectId));

        model.addAttribute("viewModel", viewModel);
        model.addAttribute("title", "Ведение журнала");
        model.addAttribute("activePage", "journal");
        model.addAttribute("content", "journal/index");

        return "layout";
    }

    @PostMapping("/save-grade")
    @ResponseBody
    @Transactional
    public Map<String, Object> saveGrade(
            @RequestParam int studentId,
            @RequestParam int lessonId,
            @RequestParam(required = false) String gradeValue,
            @RequestParam(required = false) String attendanceStatus,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) String remark) {

        Map<String, Object> response = new HashMap<>();

        try {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Ученик не найден"));
            Schedule lesson = scheduleRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Урок не найден"));

            // Если выбрана посещаемость, удаляем оценку, комментарий и замечание
            if (attendanceStatus != null && !attendanceStatus.isEmpty()) {
                // Удаляем оценку
                gradeRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId)
                        .ifPresent(grade -> gradeRepository.delete(grade));

                // Удаляем замечание
                remarkRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId)
                        .ifPresent(remarkRepo -> remarkRepository.delete(remarkRepo));

                // Сохраняем посещаемость
                Attendance attendance = attendanceRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId)
                        .orElseGet(() -> {
                            Attendance newAttendance = new Attendance();
                            newAttendance.setStudent(student);
                            newAttendance.setLesson(lesson);
                            return newAttendance;
                        });
                attendance.setStatus(attendanceStatus);
                attendanceRepository.save(attendance);

            } else {
                // Если посещаемость не выбрана, удаляем запись о посещаемости
                attendanceRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId)
                        .ifPresent(attendance -> attendanceRepository.delete(attendance));

                // Обработка оценки
                if (gradeValue != null && !gradeValue.isEmpty()) {
                    Grade grade = gradeRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId)
                            .orElseGet(() -> {
                                Grade newGrade = new Grade();
                                newGrade.setStudent(student);
                                newGrade.setLesson(lesson);
                                newGrade.setDate(LocalDateTime.now());
                                return newGrade;
                            });
                    grade.setGradeValue(Integer.parseInt(gradeValue));
                    grade.setComment(comment);
                    gradeRepository.save(grade);

                    // БЛОК ВЕДЕНИЯ ИСТОРИИ ОЦЕНОК
                    if (grade.getGradeValue() != null) {
                        int coinDelta = 0;

                        // Расценка: 5 -> +3, 4 -> +1, 3 -> 0, 2 -> -2
                        switch (grade.getGradeValue()) {
                            case 5: coinDelta = 3; break;
                            case 4: coinDelta = 1; break;
                            case 3: coinDelta = 0; break;
                            case 2: coinDelta = -2; break;
                        }

                        if (coinDelta != 0) {
                            int currentBalance = student.getCoins() != null ? student.getCoins() : 0;
                            int newBalance = currentBalance + coinDelta;

                            // Защита от ухода в минус (ученик не может задолжать школе)
                            if (newBalance < 0) {
                                newBalance = 0;
                            }

                            int actualDelta = newBalance - currentBalance;

                            // Если баланс реально изменился
                            if (actualDelta != 0) {
                                // 1. Обновляем монеты ученика
                                student.setCoins(newBalance);
                                userRepository.save(student);

                                // 2. Записываем в историю
                                TransactionHistory tx = new TransactionHistory();
                                tx.setStudent(student);
                                tx.setAmount(actualDelta);

                                if (actualDelta > 0) {
                                    tx.setDescription("Начисление за оценку " + grade.getGradeValue());
                                } else {
                                    tx.setDescription("Списание за оценку " + grade.getGradeValue());
                                }

                                tx.setGrade(grade); // Привязываем транзакцию к конкретной оценке
                                transactionHistoryRepository.save(tx);
                            }
                        }
                    }
                    
                } else {
                    gradeRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId)
                            .ifPresent(grade -> gradeRepository.delete(grade));
                }

                // Обработка замечания
                if (remark != null && !remark.isEmpty()) {
                    Remark remarkEntity = remarkRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId)
                            .orElseGet(() -> {
                                Remark newRemark = new Remark();
                                newRemark.setStudent(student);
                                newRemark.setLesson(lesson);
                                return newRemark;
                            });
                    remarkEntity.setText(remark);
                    remarkRepository.save(remarkEntity);
                } else {
                    remarkRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId)
                            .ifPresent(remarkRepo -> remarkRepository.delete(remarkRepo));
                }
            }

            response.put("success", true);
            response.put("message", "Данные успешно сохранены");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/clear-grade")
    @ResponseBody
    @Transactional
    public Map<String, Object> clearGrade(
            @RequestParam int studentId,
            @RequestParam int lessonId) {

        Map<String, Object> response = new HashMap<>();

        try {
            gradeRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId)
                    .ifPresent(grade -> gradeRepository.delete(grade));
            attendanceRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId)
                    .ifPresent(attendance -> attendanceRepository.delete(attendance));
            remarkRepository.findByStudentUserIdAndLessonLessonId(studentId, lessonId)
                    .ifPresent(remark -> remarkRepository.delete(remark));

            response.put("success", true);
            response.put("message", "Все данные успешно очищены");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка очистки: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/lesson-form")
    public String lessonForm(@RequestParam int lessonId, Model model) {
        Schedule lesson = scheduleRepository.findById(lessonId).orElse(null);
        model.addAttribute("lesson", lesson);
        return "journal/_lesson_form_partial";
    }

    @PostMapping("/save-lesson")
    @ResponseBody
    @Transactional
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
            response.put("message", "Данные урока сохранены");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }

        return response;
    }

    private Map<String, String> generateAvailableWeeks() {
        Map<String, String> weeks = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate currentMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Начинаем с 1 сентября текущего года
        int year = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
        LocalDate academicYearStart = LocalDate.of(year, 9, 1);
        LocalDate start = academicYearStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Заканчиваем текущей неделей
        LocalDate end = currentMonday;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        int weekNum = 1;

        LocalDate week = start;
        while (!week.isAfter(end)) {
            String key = week.toString();
            String value = weekNum + " нед. (" + week.format(formatter) + " - " + week.plusDays(6).format(formatter) + ")";
            weeks.put(key, value);
            week = week.plusWeeks(1);
            weekNum++;

            if (weekNum > 52) break;
        }

        return weeks;
    }
}