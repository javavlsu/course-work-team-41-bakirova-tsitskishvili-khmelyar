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
import java.time.Month;
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
    private StudentClassRepository studentClassRepository;

    // Заглушки данных для демонстрации (временные, пока нет данных в БД)
    private List<Schedule> schedules = new ArrayList<>();
    private Map<Integer, List<User>> classStudents = new HashMap<>();
    private Map<Integer, List<Subject>> teacherSubjects = new HashMap<>();
    private Map<Integer, List<SchoolClass>> teacherClasses = new HashMap<>();
    private Map<String, GradeData> gradeDataMap = new HashMap<>();

    public JournalController() {
        initializeDemoData();
    }

    private void initializeDemoData() {
        // Инициализация расписания (только для демо)
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (int i = 0; i < 5; i++) {
            LocalDate lessonDate = monday.plusDays(i);
            Schedule schedule = new Schedule();
            schedule.setLessonId(i + 1);

            // Создаем заглушки для связанных объектов
            SchoolClass schoolClass = new SchoolClass();
            schoolClass.setClassId(1);
            schoolClass.setClassNumber(9);
            schoolClass.setClassLetter("А");
            schedule.setSchoolClass(schoolClass);

            Subject subject = new Subject();
            subject.setSubjectId(1);
            subject.setSubjectName("Математика");
            schedule.setSubject(subject);

            User teacher = new User();
            teacher.setUserId(1);
            teacher.setFirstName("Алексей");
            teacher.setLastName("Иванов");
            schedule.setTeacher(teacher);

            schedule.setLessonDateTime(lessonDate.atTime(8, 30));
            schedule.setLessonTopic("Тема урока " + (i + 1));
            schedule.setHomeworkText("Домашнее задание " + (i + 1));
            schedules.add(schedule);
        }

        // Инициализация учеников (для демо)
        List<User> students9A = new ArrayList<>();

        User student1 = new User();
        student1.setUserId(1);
        student1.setLastName("Иванов");
        student1.setFirstName("Алексей");
        student1.setMiddleName("Петрович");
        students9A.add(student1);

        User student2 = new User();
        student2.setUserId(2);
        student2.setLastName("Петрова");
        student2.setFirstName("Мария");
        student2.setMiddleName("Сергеевна");
        students9A.add(student2);

        User student3 = new User();
        student3.setUserId(3);
        student3.setLastName("Сидоров");
        student3.setFirstName("Дмитрий");
        student3.setMiddleName("Иванович");
        students9A.add(student3);

        User student4 = new User();
        student4.setUserId(4);
        student4.setLastName("Кузнецова");
        student4.setFirstName("Анна");
        student4.setMiddleName("Владимировна");
        students9A.add(student4);

        classStudents.put(1, students9A);

        // Инициализация предметов для учителя
        List<Subject> teacherSubjs = new ArrayList<>();

        Subject math = new Subject();
        math.setSubjectId(1);
        math.setSubjectName("Математика");
        teacherSubjs.add(math);

        Subject russian = new Subject();
        russian.setSubjectId(2);
        russian.setSubjectName("Русский язык");
        teacherSubjs.add(russian);

        Subject physics = new Subject();
        physics.setSubjectId(3);
        physics.setSubjectName("Физика");
        teacherSubjs.add(physics);

        teacherSubjects.put(1, teacherSubjs);

        // Инициализация классов для учителя
        List<SchoolClass> teacherCls = new ArrayList<>();

        SchoolClass class9A = new SchoolClass();
        class9A.setClassId(1);
        class9A.setClassNumber(9);
        class9A.setClassLetter("А");
        teacherCls.add(class9A);

        SchoolClass class9B = new SchoolClass();
        class9B.setClassId(2);
        class9B.setClassNumber(9);
        class9B.setClassLetter("Б");
        teacherCls.add(class9B);

        SchoolClass class10A = new SchoolClass();
        class10A.setClassId(3);
        class10A.setClassNumber(10);
        class10A.setClassLetter("А");
        teacherCls.add(class10A);

        teacherClasses.put(1, teacherCls);
    }

    @GetMapping("/index")
    public String index(
            @RequestParam(value = "classId", required = false) Integer classId,
            @RequestParam(value = "subjectId", required = false) Integer subjectId,
            @RequestParam(value = "weekStart", required = false) String weekStartStr,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Получаем текущего пользователя
        Optional<User> currentUserOpt = userRepository.findByLogin(username);
        if (!currentUserOpt.isPresent()) {
            return "redirect:/login";
        }

        User currentUser = currentUserOpt.get();
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

        LocalDate academicYearStart = LocalDate.of(today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1,
                9, 1);
        LocalDate firstAcademicMonday = academicYearStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Map<String, String> availableWeeks = new LinkedHashMap<>();
        LocalDate week = firstAcademicMonday;
        int weekNumber = 1;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        while (!week.isAfter(currentMonday.plusWeeks(1))) {
            LocalDate weekEnd = week.plusDays(6);
            String weekLabel = weekNumber + " нед. (" + week.format(formatter) + " - " + weekEnd.format(formatter) + ")";
            availableWeeks.put(week.toString(), weekLabel);
            week = week.plusWeeks(1);
            weekNumber++;
        }

        Map<Integer, String> availableClasses = new LinkedHashMap<>();
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
            List<SchoolClass> teacherCls = teacherClasses.getOrDefault(currentUser.getUserId(), new ArrayList<>());
            for (SchoolClass cls : teacherCls) {
                availableClasses.put(cls.getClassId(), cls.getClassName());
            }

            List<Subject> teacherSubjs = teacherSubjects.getOrDefault(currentUser.getUserId(), new ArrayList<>());
            for (Subject subj : teacherSubjs) {
                availableSubjects.put(subj.getSubjectId(), subj.getSubjectName());
            }
        }

        // Если списки пустые, добавляем заглушки
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

        int selectedClassId = classId != null ? classId : availableClasses.keySet().iterator().next();
        int selectedSubjectId = subjectId != null ? subjectId : availableSubjects.keySet().iterator().next();

        final LocalDate filterWeekStart = weekStart;
        final LocalDate filterWeekEnd = weekStart.plusDays(7);

        // Получаем уроки для выбранного класса и недели
        List<Schedule> lessonsForWeek = schedules.stream()
                .filter(s -> {
                    SchoolClass sc = s.getSchoolClass();
                    Subject subj = s.getSubject();
                    return sc != null && sc.getClassId() == selectedClassId &&
                            subj != null && subj.getSubjectId() == selectedSubjectId &&
                            s.getLessonDateTime() != null &&
                            s.getLessonDateTime().toLocalDate().isAfter(filterWeekStart.minusDays(1)) &&
                            s.getLessonDateTime().toLocalDate().isBefore(filterWeekEnd);
                })
                .sorted(Comparator.comparing(Schedule::getLessonDateTime))
                .collect(Collectors.toList());

        // Получаем учеников класса
        List<User> students = classStudents.getOrDefault(selectedClassId, new ArrayList<>());

        List<JournalRow> rows = new ArrayList<>();
        for (User student : students) {
            JournalRow row = new JournalRow();
            row.setStudent(student);

            for (Schedule lesson : lessonsForWeek) {
                // Проверяем сохраненные данные в gradeDataMap
                String key = student.getUserId() + "_" + lesson.getLessonId();
                CellData cell;

                if (gradeDataMap.containsKey(key)) {
                    GradeData gradeData = gradeDataMap.get(key);
                    cell = new CellData();
                    if (gradeData.getAttendanceStatus() != null && !gradeData.getAttendanceStatus().isEmpty()) {
                        cell.setValue(gradeData.getAttendanceStatus());
                        cell.setAttendance(true);
                    } else if (gradeData.getGradeValue() != null && !gradeData.getGradeValue().isEmpty()) {
                        cell.setValue(gradeData.getGradeValue());
                        cell.setAttendance(false);
                    }
                    if (gradeData.getComment() != null && !gradeData.getComment().isEmpty()) {
                        cell.setHasComment(true);
                        cell.setComment(gradeData.getComment());
                    }
                } else {
                    // Генерируем демо-данные
                    cell = generateDemoCellData(student.getUserId(), lesson.getLessonId());
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

    // МЕТОД: Сохранение оценки/посещаемости
    @PostMapping("/save-grade")
    @ResponseBody
    public Map<String, Object> saveGrade(
            @RequestParam int studentId,
            @RequestParam int lessonId,
            @RequestParam(required = false) String gradeValue,
            @RequestParam(required = false) String attendanceStatus,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) String weekStart) {

        Map<String, Object> response = new HashMap<>();

        try {
            String key = studentId + "_" + lessonId;
            GradeData gradeData = new GradeData(studentId, lessonId, gradeValue, attendanceStatus, comment);
            gradeDataMap.put(key, gradeData);

            System.out.println("Сохранение данных: studentId=" + studentId +
                    ", lessonId=" + lessonId +
                    ", grade=" + gradeValue +
                    ", attendance=" + attendanceStatus +
                    ", comment=" + (comment != null ? comment.substring(0, Math.min(comment.length(), 50)) : "null"));

            response.put("success", true);
            response.put("message", "Данные успешно сохранены");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка сохранения: " + e.getMessage());
        }

        return response;
    }

    // МЕТОД: Очистка оценки
    @PostMapping("/clear-grade")
    @ResponseBody
    public Map<String, Object> clearGrade(
            @RequestParam int studentId,
            @RequestParam int lessonId) {

        Map<String, Object> response = new HashMap<>();

        try {
            String key = studentId + "_" + lessonId;
            gradeDataMap.remove(key);

            System.out.println("Очистка данных: studentId=" + studentId + ", lessonId=" + lessonId);

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
        Schedule lesson = schedules.stream()
                .filter(s -> s.getLessonId() == lessonId)
                .findFirst()
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
            Optional<Schedule> optionalLesson = schedules.stream()
                    .filter(s -> s.getLessonId() == lessonId)
                    .findFirst();

            if (optionalLesson.isPresent()) {
                Schedule lesson = optionalLesson.get();
                lesson.setLessonTopic(lessonTopic);
                lesson.setHomeworkText(homeworkText);
                response.put("success", true);
                response.put("message", "Данные урока успешно сохранены");
            } else {
                response.put("success", false);
                response.put("message", "Урок не найден");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка сохранения: " + e.getMessage());
        }

        return response;
    }

    private CellData generateDemoCellData(int studentId, int lessonId) {
        Random random = new Random(studentId + lessonId);
        int rand = random.nextInt(10);

        CellData cell = new CellData();

        if (rand < 2) {
            String[] statuses = {"Н", "У", "Б"};
            cell.setValue(statuses[random.nextInt(statuses.length)]);
            cell.setAttendance(true);
        } else if (rand < 8) {
            int[] grades = {2, 3, 4, 5};
            cell.setValue(String.valueOf(grades[random.nextInt(grades.length)]));
            cell.setAttendance(false);

            if (random.nextBoolean()) {
                cell.setHasComment(true);
                cell.setComment("Комментарий к оценке");
            }
        }

        return cell;
    }

    // Вспомогательный класс для хранения данных об оценках
    private static class GradeData {
        private int studentId;
        private int lessonId;
        private String gradeValue;
        private String attendanceStatus;
        private String comment;
        private LocalDateTime createdDate;

        public GradeData(int studentId, int lessonId, String gradeValue, String attendanceStatus, String comment) {
            this.studentId = studentId;
            this.lessonId = lessonId;
            this.gradeValue = gradeValue;
            this.attendanceStatus = attendanceStatus;
            this.comment = comment;
            this.createdDate = LocalDateTime.now();
        }

        public int getStudentId() { return studentId; }
        public int getLessonId() { return lessonId; }
        public String getGradeValue() { return gradeValue; }
        public String getAttendanceStatus() { return attendanceStatus; }
        public String getComment() { return comment; }
        public LocalDateTime getCreatedDate() { return createdDate; }
    }
}