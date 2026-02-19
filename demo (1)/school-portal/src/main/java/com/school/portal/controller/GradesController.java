package com.school.portal.controller;

import com.school.portal.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Controller
public class GradesController {

    // Заглушка данных для демонстрации
    private Map<String, List<StudentSubjectItem>> studentData = new HashMap<>();
    private Map<String, Map<Integer, List<TeacherStudentGradeItem>>> teacherData = new HashMap<>();

    public GradesController() {
        initializeDemoData();
    }

    private void initializeDemoData() {
        // Данные для ученика
        List<StudentSubjectItem> studentSubjects = new ArrayList<>();

        StudentSubjectItem math = new StudentSubjectItem();
        math.setSubjectName("Математика");
        math.setAverageGrade(4.25);
        math.setQuarterFinalGrade(4);
        math.setTotalAbsences(3);
        math.setAbsentTypeH(1);
        math.setAbsentTypeU(1);
        math.setAbsentTypeB(1);
        math.setTotalLessonsInPeriod(45);
        math.setAllGrades(Arrays.asList(5, 4, 5, 3, 5, 4));
        studentSubjects.add(math);

        StudentSubjectItem russian = new StudentSubjectItem();
        russian.setSubjectName("Русский язык");
        russian.setAverageGrade(3.83);
        russian.setQuarterFinalGrade(4);
        russian.setTotalAbsences(1);
        russian.setAbsentTypeH(0);
        russian.setAbsentTypeU(1);
        russian.setAbsentTypeB(0);
        russian.setTotalLessonsInPeriod(40);
        russian.setAllGrades(Arrays.asList(4, 3, 4, 4, 5, 3));
        studentSubjects.add(russian);

        StudentSubjectItem history = new StudentSubjectItem();
        history.setSubjectName("История");
        history.setAverageGrade(4.5);
        history.setQuarterFinalGrade(5);
        history.setTotalAbsences(0);
        history.setAbsentTypeH(0);
        history.setAbsentTypeU(0);
        history.setAbsentTypeB(0);
        history.setTotalLessonsInPeriod(30);
        history.setAllGrades(Arrays.asList(5, 4, 5, 5, 4));
        studentSubjects.add(history);

        StudentSubjectItem physics = new StudentSubjectItem();
        physics.setSubjectName("Физика");
        physics.setAverageGrade(2.8);
        physics.setQuarterFinalGrade(3);
        physics.setTotalAbsences(5);
        physics.setAbsentTypeH(2);
        physics.setAbsentTypeU(2);
        physics.setAbsentTypeB(1);
        physics.setTotalLessonsInPeriod(35);
        physics.setAllGrades(Arrays.asList(3, 2, 3, 4, 2));
        studentSubjects.add(physics);

        studentData.put("student", studentSubjects);
        studentData.put("parent", studentSubjects); // Родитель видит те же данные

        // Данные для учителя
        Map<Integer, List<TeacherStudentGradeItem>> class1Data = new HashMap<>();

        List<TeacherStudentGradeItem> class1Students = new ArrayList<>();

        // Студент 1
        TeacherStudentGradeItem student1 = new TeacherStudentGradeItem();
        student1.setStudentId(1);
        student1.setFullName("Иванов Алексей Петрович");
        student1.setAverageGrade(4.67);
        student1.setQuarterFinalGrade(5);
        student1.setTotalAbsences(2);
        student1.setAbsentTypeH(1);
        student1.setAbsentTypeU(1);
        student1.setAbsentTypeB(0);
        student1.setTotalLessonsInPeriod(45);
        student1.setAllGrades(Arrays.asList(5, 5, 4, 5, 5, 4));
        class1Students.add(student1);

        // Студент 2
        TeacherStudentGradeItem student2 = new TeacherStudentGradeItem();
        student2.setStudentId(2);
        student2.setFullName("Петрова Мария Сергеевна");
        student2.setAverageGrade(3.83);
        student2.setQuarterFinalGrade(4);
        student2.setTotalAbsences(3);
        student2.setAbsentTypeH(0);
        student2.setAbsentTypeU(2);
        student2.setAbsentTypeB(1);
        student2.setTotalLessonsInPeriod(45);
        student2.setAllGrades(Arrays.asList(4, 3, 4, 5, 4, 3));
        class1Students.add(student2);

        // Студент 3
        TeacherStudentGradeItem student3 = new TeacherStudentGradeItem();
        student3.setStudentId(3);
        student3.setFullName("Сидоров Дмитрий Иванович");
        student3.setAverageGrade(2.5);
        student3.setQuarterFinalGrade(3);
        student3.setTotalAbsences(7);
        student3.setAbsentTypeH(3);
        student3.setAbsentTypeU(2);
        student3.setAbsentTypeB(2);
        student3.setTotalLessonsInPeriod(45);
        student3.setAllGrades(Arrays.asList(3, 2, 3, 2, 3, 2));
        class1Students.add(student3);

        class1Data.put(1, class1Students); // ID предмета 1 (Математика)

        teacherData.put("1", class1Data); // ID класса 1 (9 "А")
    }

    @GetMapping("/grades")
    public String index(
            @RequestParam(value = "classId", required = false) Integer classId,
            @RequestParam(value = "subjectId", required = false) Integer subjectId,
            @RequestParam(value = "quarter", required = false, defaultValue = "I") String quarter,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String role = getCurrentUserRole(auth);

        model.addAttribute("title", "Успеваемость");
        model.addAttribute("activePage", "grades");

        // Устанавливаем доступные четверти
        List<String> availableQuarters = Arrays.asList("Итоговые оценки", "I", "II", "III", "IV");
        model.addAttribute("availableQuarters", availableQuarters);
        model.addAttribute("selectedQuarter", quarter);

        // Маршрутизация по ролям
        if (role.equals("ROLE_PARENT")) {
            return parentView(model, username, quarter);
        } else if (role.equals("ROLE_STUDENT")) {
            return studentView(model, username, quarter, false);
        } else if (role.equals("ROLE_TEACHER") || role.equals("ROLE_DIRECTOR")) {
            return teacherView(model, classId, subjectId, quarter);
        }

        return "error";
    }

    private String parentView(Model model, String parentUsername, String quarter) {
        StudentGradesViewModel viewModel = new StudentGradesViewModel();
        viewModel.setStudentFullName("Петрова Мария Сергеевна");
        viewModel.setClassName("9 \"А\"");
        viewModel.setSubjects(studentData.get("parent"));
        viewModel.setSelectedQuarter(quarter);
        viewModel.setAvailableQuarters(Arrays.asList("Итоговые оценки", "I", "II", "III", "IV"));
        viewModel.setParentView(true);

        model.addAttribute("viewModel", viewModel);
        model.addAttribute("content", "grades/student-view");
        return "layout";
    }

    private String studentView(Model model, String studentUsername, String quarter, boolean isParentView) {
        StudentGradesViewModel viewModel = new StudentGradesViewModel();
        viewModel.setStudentFullName("Иванов Алексей Петрович");
        viewModel.setClassName("9 \"А\"");
        viewModel.setSubjects(studentData.get("student"));
        viewModel.setSelectedQuarter(quarter);
        viewModel.setAvailableQuarters(Arrays.asList("Итоговые оценки", "I", "II", "III", "IV"));
        viewModel.setParentView(false);

        model.addAttribute("viewModel", viewModel);
        model.addAttribute("content", "grades/student-view");
        return "layout";
    }

    private String teacherView(Model model, Integer classId, Integer subjectId, String quarter) {
        TeacherClassGradesViewModel viewModel = new TeacherClassGradesViewModel();

        // Доступные классы (заглушка)
        Map<Integer, String> availableClasses = new LinkedHashMap<>();
        availableClasses.put(1, "9 \"А\"");
        availableClasses.put(2, "9 \"Б\"");
        availableClasses.put(3, "10 \"А\"");
        availableClasses.put(4, "10 \"Б\"");
        availableClasses.put(5, "11 \"А\"");

        // Доступные предметы (заглушка)
        Map<Integer, String> availableSubjects = new LinkedHashMap<>();
        availableSubjects.put(1, "Математика");
        availableSubjects.put(2, "Русский язык");
        availableSubjects.put(3, "Физика");
        availableSubjects.put(4, "Химия");
        availableSubjects.put(5, "История");

        // Устанавливаем значения по умолчанию, если не указаны
        int currentClassId = classId != null ? classId : 1;
        int currentSubjectId = subjectId != null ? subjectId : 1;

        // Получаем данные для выбранного класса и предмета
        List<TeacherStudentGradeItem> students = teacherData.getOrDefault(
                String.valueOf(currentClassId),
                new HashMap<>()
        ).getOrDefault(currentSubjectId, new ArrayList<>());

        viewModel.setStudents(students);
        viewModel.setAvailableClasses(availableClasses);
        viewModel.setAvailableSubjects(availableSubjects);
        viewModel.setAvailableQuarters(Arrays.asList("Итоговые оценки", "I", "II", "III", "IV"));
        viewModel.setSelectedClassId(currentClassId);
        viewModel.setSelectedSubjectId(currentSubjectId);
        viewModel.setSelectedQuarter(quarter);
        viewModel.setSelectedClassName(availableClasses.get(currentClassId));
        viewModel.setSelectedSubjectName(availableSubjects.get(currentSubjectId));

        model.addAttribute("viewModel", viewModel);
        model.addAttribute("content", "grades/teacher-view");
        return "layout";
    }

    private String getCurrentUserRole(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "ROLE_ANONYMOUS";
        }

        // Получаем первую роль из authorities
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");
    }

    // Вспомогательные методы для расчета дат (заглушки)
    private LocalDate getQuarterStartDate(String quarter) {
        int currentYear = LocalDate.now().getYear();

        switch (quarter) {
            case "I":
                return LocalDate.of(currentYear, Month.SEPTEMBER, 1);
            case "II":
                return LocalDate.of(currentYear, Month.NOVEMBER, 1);
            case "III":
                return LocalDate.of(currentYear + 1, Month.JANUARY, 15);
            case "IV":
                return LocalDate.of(currentYear + 1, Month.APRIL, 1);
            default: // Итоговые оценки
                return LocalDate.of(currentYear, Month.SEPTEMBER, 1);
        }
    }

    private LocalDate getQuarterEndDate(String quarter) {
        int currentYear = LocalDate.now().getYear();

        switch (quarter) {
            case "I":
                return LocalDate.of(currentYear, Month.OCTOBER, 31);
            case "II":
                return LocalDate.of(currentYear, Month.DECEMBER, 31);
            case "III":
                return LocalDate.of(currentYear + 1, Month.MARCH, 31);
            case "IV":
                return LocalDate.of(currentYear + 1, Month.MAY, 25);
            default: // Итоговые оценки
                return LocalDate.of(currentYear + 1, Month.JUNE, 30);
        }
    }
}