package com.school.portal.controller;

import com.school.portal.model.*;
import com.school.portal.model.dto.*;
import com.school.portal.repository.*;
import com.school.portal.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class GradesController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SchoolClassRepository classRepository;

    @Autowired
    private StudentClassRepository studentClassRepository;

    @Autowired
    private GradeService gradeService;

    @GetMapping("/grades")
    public String index(
            @RequestParam(value = "classId", required = false) Integer classId,
            @RequestParam(value = "subjectId", required = false) Integer subjectId,
            @RequestParam(value = "quarter", required = false, defaultValue = "I") String quarter,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String role = getCurrentUserRole(auth);

        model.addAttribute("title", "Успеваемость");
        model.addAttribute("activePage", "grades");

        // Устанавливаем доступные четверти
        List<String> availableQuarters = Arrays.asList("Итоговые оценки", "I", "II", "III", "IV");
        model.addAttribute("availableQuarters", availableQuarters);
        model.addAttribute("selectedQuarter", quarter);

        // Получаем даты четверти
        LocalDateTime[] quarterDates = getQuarterDates(quarter);
        LocalDateTime startDate = quarterDates[0];
        LocalDateTime endDate = quarterDates[1];

        // Маршрутизация по ролям
        if (role.equals("ROLE_PARENT")) {
            return parentView(model, currentUser, quarter, startDate, endDate);
        } else if (role.equals("ROLE_STUDENT")) {
            return studentView(model, currentUser, quarter, startDate, endDate);
        } else if (role.equals("ROLE_TEACHER") || role.equals("ROLE_DIRECTOR")) {
            return teacherView(model, currentUser, classId, subjectId, quarter, startDate, endDate);
        }

        return "error";
    }

    private String parentView(Model model, User parent, String quarter,
                              LocalDateTime startDate, LocalDateTime endDate) {
        // Находим детей родителя (упрощенно - первый найденный)
        // В реальном проекте нужно через StudentParentRepository
        User student = findStudentForParent(parent.getUserId());

        if (student == null) {
            model.addAttribute("errorMessage", "Не найден ребенок");
            return "error";
        }

        return buildStudentGradesView(model, student, quarter, startDate, endDate, true);
    }

    private String studentView(Model model, User student, String quarter,
                               LocalDateTime startDate, LocalDateTime endDate) {
        return buildStudentGradesView(model, student, quarter, startDate, endDate, false);
    }

    private String buildStudentGradesView(Model model, User student, String quarter,
                                          LocalDateTime startDate, LocalDateTime endDate, boolean isParentView) {

        // Получаем класс ученика
        Optional<StudentClass> studentClassOpt = studentClassRepository.findByStudentUserId(student.getUserId());
        String className = studentClassOpt.map(sc -> sc.getSchoolClass().getClassName()).orElse("Не определен");

        // Получаем все предметы
        List<Subject> allSubjects = subjectRepository.findAll();

        List<StudentSubjectItem> subjectItems = new ArrayList<>();

        for (Subject subject : allSubjects) {
            // Получаем оценки по предмету за период
            List<Grade> grades = gradeRepository.findGradesForStudentBySubjectAndPeriod(
                    student.getUserId(), subject.getSubjectId(), startDate, endDate);

            StudentSubjectItem item = new StudentSubjectItem();
            item.setSubjectName(subject.getSubjectName());

            if (!grades.isEmpty()) {
                // Средний балл
                double avg = grades.stream()
                        .filter(g -> g.getGradeValue() != null)
                        .mapToInt(Grade::getGradeValue)
                        .average()
                        .orElse(0);
                item.setAverageGrade(avg);

                // Все оценки
                List<Integer> gradeValues = grades.stream()
                        .filter(g -> g.getGradeValue() != null)
                        .map(Grade::getGradeValue)
                        .collect(Collectors.toList());
                item.setAllGrades(gradeValues);

                // Итоговая оценка (упрощенно - средняя)
                item.setQuarterFinalGrade((int) Math.round(avg));
            }

            // Пропуски (нужно через AttendanceRepository)
            // item.setTotalAbsences(...);

            subjectItems.add(item);
        }

        StudentGradesViewModel viewModel = new StudentGradesViewModel();
        viewModel.setStudentFullName(student.getFullName());
        viewModel.setClassName(className);
        viewModel.setSubjects(subjectItems);
        viewModel.setSelectedQuarter(quarter);
        viewModel.setAvailableQuarters(Arrays.asList("Итоговые оценки", "I", "II", "III", "IV"));
        viewModel.setParentView(isParentView);

        model.addAttribute("viewModel", viewModel);
        model.addAttribute("content", "grades/student-view");
        return "layout";
    }

    private String teacherView(Model model, User teacher, Integer classId, Integer subjectId,
                               String quarter, LocalDateTime startDate, LocalDateTime endDate) {

        // Доступные классы для учителя
        List<SchoolClass> teacherClasses = classRepository.findClassesByTeacherId(teacher.getUserId());
        Map<Integer, String> availableClasses = new LinkedHashMap<>();
        for (SchoolClass sc : teacherClasses) {
            availableClasses.put(sc.getClassId(), sc.getClassName());
        }

        // Доступные предметы для учителя
        List<Subject> teacherSubjects = subjectRepository.findSubjectsByTeacherId(teacher.getUserId());
        Map<Integer, String> availableSubjects = new LinkedHashMap<>();
        for (Subject subj : teacherSubjects) {
            availableSubjects.put(subj.getSubjectId(), subj.getSubjectName());
        }

        // Если списки пустые (директор), добавляем все
        if (availableClasses.isEmpty()) {
            List<SchoolClass> allClasses = classRepository.findAll();
            for (SchoolClass sc : allClasses) {
                availableClasses.put(sc.getClassId(), sc.getClassName());
            }
        }

        if (availableSubjects.isEmpty()) {
            List<Subject> allSubjects = subjectRepository.findAll();
            for (Subject subj : allSubjects) {
                availableSubjects.put(subj.getSubjectId(), subj.getSubjectName());
            }
        }

        // Устанавливаем значения по умолчанию
        int currentClassId = classId != null && availableClasses.containsKey(classId) ?
                classId : availableClasses.keySet().iterator().next();
        int currentSubjectId = subjectId != null && availableSubjects.containsKey(subjectId) ?
                subjectId : availableSubjects.keySet().iterator().next();

        // Получаем учеников класса
        List<User> students = studentClassRepository.findStudentsByClassId(currentClassId)
                .stream()
                .map(obj -> (User) obj)
                .collect(Collectors.toList());

        List<TeacherStudentGradeItem> studentItems = new ArrayList<>();

        for (User student : students) {
            List<Grade> grades = gradeRepository.findGradesForStudentBySubjectAndPeriod(
                    student.getUserId(), currentSubjectId, startDate, endDate);

            TeacherStudentGradeItem item = new TeacherStudentGradeItem();
            item.setStudentId(student.getUserId());
            item.setFullName(student.getFullName());

            if (!grades.isEmpty()) {
                double avg = grades.stream()
                        .filter(g -> g.getGradeValue() != null)
                        .mapToInt(Grade::getGradeValue)
                        .average()
                        .orElse(0);
                item.setAverageGrade(avg);

                List<Integer> gradeValues = grades.stream()
                        .filter(g -> g.getGradeValue() != null)
                        .map(Grade::getGradeValue)
                        .collect(Collectors.toList());
                item.setAllGrades(gradeValues);

                item.setQuarterFinalGrade((int) Math.round(avg));
            }

            studentItems.add(item);
        }

        TeacherClassGradesViewModel viewModel = new TeacherClassGradesViewModel();
        viewModel.setStudents(studentItems);
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

    private LocalDateTime[] getQuarterDates(String quarter) {
        int year = LocalDate.now().getYear();
        LocalDateTime startDate, endDate;

        switch (quarter) {
            case "I":
                startDate = LocalDate.of(year, Month.SEPTEMBER, 1).atStartOfDay();
                endDate = LocalDate.of(year, Month.OCTOBER, 31).atTime(23, 59, 59);
                break;
            case "II":
                startDate = LocalDate.of(year, Month.NOVEMBER, 1).atStartOfDay();
                endDate = LocalDate.of(year, Month.DECEMBER, 31).atTime(23, 59, 59);
                break;
            case "III":
                startDate = LocalDate.of(year + 1, Month.JANUARY, 15).atStartOfDay();
                endDate = LocalDate.of(year + 1, Month.MARCH, 31).atTime(23, 59, 59);
                break;
            case "IV":
                startDate = LocalDate.of(year + 1, Month.APRIL, 1).atStartOfDay();
                endDate = LocalDate.of(year + 1, Month.MAY, 25).atTime(23, 59, 59);
                break;
            default: // Итоговые оценки - весь год
                startDate = LocalDate.of(year, Month.SEPTEMBER, 1).atStartOfDay();
                endDate = LocalDate.of(year + 1, Month.JUNE, 30).atTime(23, 59, 59);
        }

        return new LocalDateTime[]{startDate, endDate};
    }

    private User findStudentForParent(int parentId) {
        // Упрощенно - первый ученик
        // В реальном проекте нужно через StudentParentRepository
        return userRepository.findByRole_RoleName("STUDENT").stream().findFirst().orElse(null);
    }

    private String getCurrentUserRole(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "ROLE_ANONYMOUS";
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");
    }
}