package com.school.portal.controller;

import com.school.portal.model.*;
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

    // Заглушки данных для демонстрации
    private List<Schedule> schedules = new ArrayList<>();
    private Map<Integer, List<Student>> classStudents = new HashMap<>();
    private Map<Integer, List<Subject>> teacherSubjects = new HashMap<>();
    private Map<Integer, List<SchoolClass>> teacherClasses = new HashMap<>();

    // Дополнительная заглушка для хранения оценок
    private Map<String, GradeData> gradeDataMap = new HashMap<>();

    public JournalController() {
        initializeDemoData();
    }

    private void initializeDemoData() {
        // Инициализация расписания
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (int i = 0; i < 5; i++) {
            LocalDate lessonDate = monday.plusDays(i);
            Schedule schedule = new Schedule();
            schedule.setLessonId(i + 1);
            schedule.setClassId(1);
            schedule.setSubjectId(1);
            schedule.setDate(lessonDate.atTime(8, 30));
            schedule.setLessonTopic("Тема урока " + (i + 1));
            schedule.setHomeworkText("Домашнее задание " + (i + 1));
            schedules.add(schedule);
        }

        // Инициализация учеников
        List<Student> students9A = new ArrayList<>();
        students9A.add(new Student(1, "Иванов", "Алексей", "Петрович"));
        students9A.add(new Student(2, "Петрова", "Мария", "Сергеевна"));
        students9A.add(new Student(3, "Сидоров", "Дмитрий", "Иванович"));
        students9A.add(new Student(4, "Кузнецова", "Анна", "Владимировна"));
        students9A.add(new Student(5, "Смирнов", "Павел", "Александрович"));
        classStudents.put(1, students9A);

        // Инициализация предметов для учителя
        List<Subject> teacherSubjs = new ArrayList<>();
        teacherSubjs.add(new Subject(1, "Математика"));
        teacherSubjs.add(new Subject(2, "Русский язык"));
        teacherSubjs.add(new Subject(3, "Физика"));
        teacherSubjects.put(1, teacherSubjs);

        // Инициализация классов для учителя
        List<SchoolClass> teacherCls = new ArrayList<>();
        teacherCls.add(new SchoolClass(1, 9, "А", 1));
        teacherCls.add(new SchoolClass(2, 9, "Б", 6));
        teacherCls.add(new SchoolClass(3, 10, "А", 1));
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
        String role = getCurrentUserRole(auth);
        boolean isDirector = role.equals("ROLE_DIRECTOR");

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
        while (!week.isAfter(currentMonday)) {
            LocalDate weekEnd = week.plusDays(6);
            String weekLabel = weekNumber + " нед. (" + week.format(formatter) + " - " + weekEnd.format(formatter) + ")";
            availableWeeks.put(week.toString(), weekLabel);
            week = week.plusWeeks(1);
            weekNumber++;
        }

        Map<Integer, String> availableClasses = new LinkedHashMap<>();
        Map<Integer, String> availableSubjects = new LinkedHashMap<>();

        if (isDirector) {
            for (int i = 1; i <= 3; i++) {
                availableClasses.put(i, (i + 8) + " \"А\"");
            }
            for (int i = 1; i <= 5; i++) {
                availableSubjects.put(i, "Предмет " + i);
            }
        } else {
            List<SchoolClass> teacherCls = teacherClasses.getOrDefault(1, new ArrayList<>());
            for (SchoolClass cls : teacherCls) {
                availableClasses.put(cls.getClassId(), cls.getClassName());
            }

            List<Subject> teacherSubjs = teacherSubjects.getOrDefault(1, new ArrayList<>());
            for (Subject subj : teacherSubjs) {
                availableSubjects.put(subj.getSubjectId(), subj.getSubjectName());
            }
        }

        int selectedClassId = classId != null ? classId : availableClasses.keySet().iterator().next();
        int selectedSubjectId = subjectId != null ? subjectId : availableSubjects.keySet().iterator().next();

        final LocalDate filterWeekStart = weekStart;
        final LocalDate filterWeekEnd = weekStart.plusDays(7);
        final int filterClassId = selectedClassId;
        final int filterSubjectId = selectedSubjectId;

        List<Schedule> lessonsForWeek = schedules.stream()
                .filter(s -> s.getClassId() == filterClassId &&
                        s.getSubjectId() == filterSubjectId &&
                        s.getDate().toLocalDate().isAfter(filterWeekStart.minusDays(1)) &&
                        s.getDate().toLocalDate().isBefore(filterWeekEnd))
                .sorted(Comparator.comparing(Schedule::getDate))
                .collect(Collectors.toList());

        List<Student> students = classStudents.getOrDefault(selectedClassId, new ArrayList<>());

        List<JournalRow> rows = new ArrayList<>();
        for (Student student : students) {
            JournalRow row = new JournalRow();
            row.setStudent(student);

            for (Schedule lesson : lessonsForWeek) {
                // Проверяем сохраненные данные в gradeDataMap
                String key = student.getUserId() + "_" + lesson.getLessonId();
                CellData cell;

                if (gradeDataMap.containsKey(key)) {
                    // Используем сохраненные данные
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

        if (lesson != null) {
            SchoolClass schoolClass = new SchoolClass(lesson.getClassId(), 9, "А", 1);
            Subject subject = new Subject(lesson.getSubjectId(), "Математика");
            lesson.setSchoolClass(schoolClass);
            lesson.setSubject(subject);
        }

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
            final String finalLessonTopic = lessonTopic;
            final String finalHomeworkText = homeworkText;

            Optional<Schedule> optionalLesson = schedules.stream()
                    .filter(s -> s.getLessonId() == lessonId)
                    .findFirst();

            if (optionalLesson.isPresent()) {
                Schedule lesson = optionalLesson.get();
                lesson.setLessonTopic(finalLessonTopic);
                lesson.setHomeworkText(finalHomeworkText);
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

    private String getCurrentUserRole(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "ROLE_ANONYMOUS";
        }

        return auth.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");
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

        // Getters and setters
        public int getStudentId() { return studentId; }
        public int getLessonId() { return lessonId; }
        public String getGradeValue() { return gradeValue; }
        public String getAttendanceStatus() { return attendanceStatus; }
        public String getComment() { return comment; }
        public LocalDateTime getCreatedDate() { return createdDate; }
    }
}