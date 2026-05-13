package com.school.portal.controller;

import com.school.portal.model.*;
import com.school.portal.repository.*;
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
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @GetMapping("/grades")
    public String index(
            @RequestParam(value = "classId", required = false) Integer classId,
            @RequestParam(value = "subjectId", required = false) Integer subjectId,
            @RequestParam(value = "quarter", required = false, defaultValue = "III") String quarter,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String roleName = getCurrentUserRole(auth);

        model.addAttribute("title", "Успеваемость");
        model.addAttribute("activePage", "grades");
        model.addAttribute("selectedQuarter", quarter);
        model.addAttribute("availableQuarters", Arrays.asList("I", "II", "III", "IV", "Итоговые оценки"));

        // Получаем даты четверти
        LocalDateTime[] dates = getQuarterDates(quarter);
        LocalDateTime startDate = dates[0];
        LocalDateTime endDate = dates[1];

        System.out.println("=== DEBUG ===");
        System.out.println("User: " + username + ", Role: " + roleName);
        System.out.println("Quarter: " + quarter + ", Date range: " + startDate + " - " + endDate);

        if ("ROLE_STUDENT".equals(roleName)) {
            return buildStudentView(model, currentUser, startDate, endDate, quarter);
        } else if ("ROLE_PARENT".equals(roleName)) {
            List<User> children = userRepository.findByRole_RoleName("STUDENT");
            if (!children.isEmpty()) {
                return buildStudentView(model, children.get(0), startDate, endDate, quarter);
            }
            model.addAttribute("errorMessage", "Ребенок не найден");
            return "error";
        } else if ("ROLE_TEACHER".equals(roleName) || "ROLE_DIRECTOR".equals(roleName) || "ROLE_ADMIN".equals(roleName)) {
            return buildTeacherView(model, currentUser, classId, subjectId, startDate, endDate, quarter);
        }

        return "error";
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

    /**
     * Расчет итоговой оценки за четверть
     */
    private int calculateQuarterGrade(Integer studentId, Integer subjectId, String quarter) {
        LocalDateTime[] dates = getQuarterDates(quarter);
        LocalDateTime startDate = dates[0];
        LocalDateTime endDate = dates[1];

        List<Grade> grades = gradeRepository.findGradesForStudentBySubjectAndPeriod(
                studentId, subjectId, startDate, endDate);

        if (grades.isEmpty()) return 0;

        double sum = 0;
        int count = 0;
        for (Grade grade : grades) {
            if (grade.getGradeValue() != null) {
                sum += grade.getGradeValue();
                count++;
            }
        }

        if (count == 0) return 0;
        return (int) Math.round(sum / count);
    }

    /**
     * Расчет годовой оценки как среднее арифметическое четвертных оценок
     * Если в четверти нет оценок, эта четверть НЕ учитывается в расчете
     */
    private int calculateYearGrade(Integer studentId, Integer subjectId) {
        List<Integer> quarterGrades = new ArrayList<>();

        // I четверть
        int grade1 = calculateQuarterGrade(studentId, subjectId, "I");
        if (grade1 > 0) quarterGrades.add(grade1);

        // II четверть
        int grade2 = calculateQuarterGrade(studentId, subjectId, "II");
        if (grade2 > 0) quarterGrades.add(grade2);

        // III четверть
        int grade3 = calculateQuarterGrade(studentId, subjectId, "III");
        if (grade3 > 0) quarterGrades.add(grade3);

        // IV четверть
        int grade4 = calculateQuarterGrade(studentId, subjectId, "IV");
        if (grade4 > 0) quarterGrades.add(grade4);

        // Если нет ни одной четвертной оценки
        if (quarterGrades.isEmpty()) return 0;

        // Среднее арифметическое только тех четвертей, где есть оценки
        double sum = 0;
        for (Integer grade : quarterGrades) {
            sum += grade;
        }
        return (int) Math.round(sum / quarterGrades.size());
    }

    private String buildStudentView(Model model, User student, LocalDateTime startDate, LocalDateTime endDate, String quarter) {
        System.out.println("=== BUILD STUDENT VIEW ===");
        System.out.println("Student ID: " + student.getUserId());

        // Получаем класс ученика
        String className = "Не определен";
        Optional<StudentClass> studentClassOpt = studentClassRepository.findByStudentUserId(student.getUserId());
        if (studentClassOpt.isPresent()) {
            SchoolClass schoolClass = studentClassOpt.get().getSchoolClass();
            className = schoolClass.getClassNumber() + " \"" + schoolClass.getClassLetter() + "\"";
        }

        // Получаем все предметы
        List<Subject> allSubjects = subjectRepository.findAll();
        System.out.println("Total subjects: " + allSubjects.size());

        List<Map<String, Object>> subjectsData = new ArrayList<>();

        for (Subject subject : allSubjects) {
            Map<String, Object> subjectData = new HashMap<>();
            subjectData.put("subjectName", subject.getSubjectName());

            // Получаем оценки по предмету за период
            List<Grade> grades = gradeRepository.findGradesForStudentBySubjectAndPeriod(
                    student.getUserId(), subject.getSubjectId(), startDate, endDate);

            System.out.println("Subject: " + subject.getSubjectName() + ", Grades found: " + grades.size());

            if (!grades.isEmpty()) {
                List<Integer> gradeValues = new ArrayList<>();
                double sum = 0;
                for (Grade grade : grades) {
                    if (grade.getGradeValue() != null) {
                        gradeValues.add(grade.getGradeValue());
                        sum += grade.getGradeValue();
                    }
                }

                double avg = gradeValues.isEmpty() ? 0 : sum / gradeValues.size();

                long count5 = gradeValues.stream().filter(g -> g == 5).count();
                long count4 = gradeValues.stream().filter(g -> g == 4).count();
                long count3 = gradeValues.stream().filter(g -> g == 3).count();
                long count2 = gradeValues.stream().filter(g -> g == 2).count();

                subjectData.put("averageGrade", avg);
                subjectData.put("allGrades", gradeValues);
                subjectData.put("gradeCount", gradeValues.size());
                subjectData.put("gradeCount5", count5);
                subjectData.put("gradeCount4", count4);
                subjectData.put("gradeCount3", count3);
                subjectData.put("gradeCount2", count2);

                // Определяем итоговую оценку
                int finalGrade;
                if ("Итоговые оценки".equals(quarter)) {
                    // Годовая оценка - среднее арифметическое четвертных
                    finalGrade = calculateYearGrade(student.getUserId(), subject.getSubjectId());
                    System.out.println("Year grade for " + subject.getSubjectName() + ": " + finalGrade);
                } else {
                    // Четвертная оценка - среднее оценок за период
                    finalGrade = (int) Math.round(avg);
                }
                subjectData.put("quarterFinalGrade", finalGrade);

            } else {
                subjectData.put("averageGrade", 0);
                subjectData.put("allGrades", new ArrayList<>());
                subjectData.put("quarterFinalGrade", 0);
                subjectData.put("gradeCount", 0);
                subjectData.put("gradeCount5", 0);
                subjectData.put("gradeCount4", 0);
                subjectData.put("gradeCount3", 0);
                subjectData.put("gradeCount2", 0);
            }

            // Считаем пропуски по конкретному предмету
            List<Schedule> lessonsForSubject = scheduleRepository.findLessonsForSubjectAndPeriod(
                    subject.getSubjectId(), startDate, endDate);

            Set<Integer> lessonIdsForSubject = lessonsForSubject.stream()
                    .map(Schedule::getLessonId)
                    .collect(Collectors.toSet());

            List<Attendance> attendances = attendanceRepository.findAttendanceForStudentInPeriod(
                    student.getUserId(), startDate, endDate);

            long absentH = 0, absentU = 0, absentB = 0;
            for (Attendance a : attendances) {
                Integer lessonId = a.getLesson().getLessonId();
                if (lessonIdsForSubject.contains(lessonId)) {
                    String status = a.getStatus();
                    if ("Н".equals(status)) {
                        absentH++;
                    } else if ("У".equals(status)) {
                        absentU++;
                    } else if ("Б".equals(status)) {
                        absentB++;
                    }
                }
            }

            long totalAbsences = absentH + absentU + absentB;

            System.out.println("Subject: " + subject.getSubjectName() +
                    ", Lessons: " + lessonIdsForSubject.size() +
                    ", Absences: " + totalAbsences +
                    " (H:" + absentH + ", U:" + absentU + ", B:" + absentB + ")");

            subjectData.put("totalAbsences", totalAbsences);
            subjectData.put("absentTypeH", absentH);
            subjectData.put("absentTypeU", absentU);
            subjectData.put("absentTypeB", absentB);

            subjectsData.add(subjectData);
        }

        model.addAttribute("studentFullName", student.getFullName());
        model.addAttribute("className", className);
        model.addAttribute("subjects", subjectsData);
        model.addAttribute("selectedQuarter", quarter);
        model.addAttribute("availableQuarters", Arrays.asList("I", "II", "III", "IV", "Итоговые оценки"));
        model.addAttribute("content", "grades/student-view");

        return "layout";
    }

    private String buildTeacherView(Model model, User teacher, Integer classId, Integer subjectId,
                                    LocalDateTime startDate, LocalDateTime endDate, String quarter) {

        System.out.println("=== BUILD TEACHER VIEW ===");
        System.out.println("Teacher ID: " + teacher.getUserId());
        System.out.println("Teacher Name: " + teacher.getFullName());
        System.out.println("Teacher Role: " + teacher.getRole().getRoleName());

        String roleName = teacher.getRole().getRoleName();
        boolean isDirectorOrAdmin = "DIRECTOR".equals(roleName) || "ADMIN".equals(roleName);

        Map<Integer, String> availableClasses = new LinkedHashMap<>();

        if (isDirectorOrAdmin) {
            // ДИРЕКТОР И АДМИН: видят ВСЕ классы
            List<SchoolClass> allClasses = classRepository.findAll();
            System.out.println("Director/Admin - loading ALL classes, found: " + allClasses.size());
            for (SchoolClass sc : allClasses) {
                availableClasses.put(sc.getClassId(), sc.getClassName());
            }
        } else {
            // УЧИТЕЛЬ: классы, где ведет уроки
            List<SchoolClass> teacherClasses = classRepository.findClassesByTeacherId(teacher.getUserId());
            System.out.println("Teacher classes (where teaches): " + teacherClasses.size());

            // + классы, где является классным руководителем
            List<SchoolClass> supervisedClasses = classRepository.findByClassTeacher(teacher);
            System.out.println("Supervised classes (homeroom teacher): " + supervisedClasses.size());

            for (SchoolClass sc : teacherClasses) {
                availableClasses.put(sc.getClassId(), sc.getClassName());
            }
            for (SchoolClass sc : supervisedClasses) {
                availableClasses.put(sc.getClassId(), sc.getClassName());
            }
        }

        if (availableClasses.isEmpty()) {
            System.out.println("WARNING: No classes found for teacher!");
            model.addAttribute("errorMessage", "У вас нет назначенных классов. Обратитесь к администратору.");
            model.addAttribute("content", "grades/teacher-view");
            return "layout";
        }

        // Выбранный класс
        Integer selectedClassId = classId;
        if (selectedClassId == null || !availableClasses.containsKey(selectedClassId)) {
            selectedClassId = availableClasses.keySet().iterator().next();
        }

        // Проверяем, является ли учитель классным руководителем выбранного класса
        boolean isHomeroomTeacher = false;
        if (!isDirectorOrAdmin) {
            List<SchoolClass> supervisedClasses = classRepository.findByClassTeacher(teacher);
            for (SchoolClass sc : supervisedClasses) {
                if (sc.getClassId().equals(selectedClassId)) {
                    isHomeroomTeacher = true;
                    break;
                }
            }
        }

        System.out.println("Selected Class ID: " + selectedClassId);
        System.out.println("Is homeroom teacher: " + isHomeroomTeacher);

        // Формируем список предметов для выбора
        Map<Integer, String> availableSubjects = new LinkedHashMap<>();

        if (isDirectorOrAdmin) {
            // ДИРЕКТОР И АДМИН: видят ВСЕ предметы
            List<Subject> allSubjects = subjectRepository.findAll();
            System.out.println("Director/Admin - loading ALL subjects, found: " + allSubjects.size());
            for (Subject subj : allSubjects) {
                availableSubjects.put(subj.getSubjectId(), subj.getSubjectName());
            }
        } else if (isHomeroomTeacher) {
            // КЛАССНЫЙ РУКОВОДИТЕЛЬ: видит ВСЕ предметы своего класса
            System.out.println("Homeroom teacher - loading ALL subjects for class");
            List<Subject> allSubjects = subjectRepository.findAll();
            for (Subject subj : allSubjects) {
                availableSubjects.put(subj.getSubjectId(), subj.getSubjectName());
            }
        } else {
            // ОБЫЧНЫЙ УЧИТЕЛЬ: видит только свои предметы
            System.out.println("Regular teacher - loading only taught subjects");
            List<Subject> teacherSubjects = subjectRepository.findSubjectsByTeacherId(teacher.getUserId());
            for (Subject subj : teacherSubjects) {
                availableSubjects.put(subj.getSubjectId(), subj.getSubjectName());
            }
        }

        if (availableSubjects.isEmpty()) {
            System.out.println("WARNING: No subjects found!");
            model.addAttribute("errorMessage", "Нет доступных предметов для отображения.");
            model.addAttribute("availableClasses", availableClasses);
            model.addAttribute("content", "grades/teacher-view");
            return "layout";
        }

        // Выбранный предмет
        Integer selectedSubjectId = subjectId;
        if (selectedSubjectId == null || !availableSubjects.containsKey(selectedSubjectId)) {
            selectedSubjectId = availableSubjects.keySet().iterator().next();
        }

        System.out.println("Selected Subject ID: " + selectedSubjectId);
        System.out.println("Available subjects: " + availableSubjects);

        // Получаем учеников класса
        List<User> students = studentClassRepository.findStudentsByClassId(selectedClassId);
        System.out.println("Students in class: " + students.size());

        List<Map<String, Object>> studentsData = new ArrayList<>();

        for (User student : students) {
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("studentId", student.getUserId());
            studentData.put("fullName", student.getFullName());

            // Получаем оценки по выбранному предмету
            List<Grade> grades = gradeRepository.findGradesForStudentBySubjectAndPeriod(
                    student.getUserId(), selectedSubjectId, startDate, endDate);

            if (!grades.isEmpty()) {
                List<Integer> gradeValues = new ArrayList<>();
                double sum = 0;
                for (Grade grade : grades) {
                    if (grade.getGradeValue() != null) {
                        gradeValues.add(grade.getGradeValue());
                        sum += grade.getGradeValue();
                    }
                }
                double avg = gradeValues.isEmpty() ? 0 : sum / gradeValues.size();

                long count5 = gradeValues.stream().filter(g -> g == 5).count();
                long count4 = gradeValues.stream().filter(g -> g == 4).count();
                long count3 = gradeValues.stream().filter(g -> g == 3).count();
                long count2 = gradeValues.stream().filter(g -> g == 2).count();

                studentData.put("averageGrade", avg);
                studentData.put("allGrades", gradeValues);
                studentData.put("gradeCount", gradeValues.size());
                studentData.put("gradeCount5", count5);
                studentData.put("gradeCount4", count4);
                studentData.put("gradeCount3", count3);
                studentData.put("gradeCount2", count2);

                // Определяем итоговую оценку
                int finalGrade;
                if ("Итоговые оценки".equals(quarter)) {
                    // Годовая оценка - среднее арифметическое четвертных
                    finalGrade = calculateYearGrade(student.getUserId(), selectedSubjectId);
                } else {
                    // Четвертная оценка - среднее оценок за период
                    finalGrade = (int) Math.round(avg);
                }
                studentData.put("quarterFinalGrade", finalGrade);

            } else {
                studentData.put("averageGrade", 0);
                studentData.put("allGrades", new ArrayList<>());
                studentData.put("quarterFinalGrade", 0);
                studentData.put("gradeCount", 0);
                studentData.put("gradeCount5", 0);
                studentData.put("gradeCount4", 0);
                studentData.put("gradeCount3", 0);
                studentData.put("gradeCount2", 0);
            }

            // Считаем пропуски по выбранному предмету
            List<Schedule> lessonsForSubject = scheduleRepository.findLessonsForSubjectAndPeriod(
                    selectedSubjectId, startDate, endDate);

            Set<Integer> lessonIdsForSubject = lessonsForSubject.stream()
                    .map(Schedule::getLessonId)
                    .collect(Collectors.toSet());

            List<Attendance> attendances = attendanceRepository.findAttendanceForStudentInPeriod(
                    student.getUserId(), startDate, endDate);

            long absentH = 0, absentU = 0, absentB = 0;
            for (Attendance a : attendances) {
                Integer lessonId = a.getLesson().getLessonId();
                if (lessonIdsForSubject.contains(lessonId)) {
                    String status = a.getStatus();
                    if ("Н".equals(status)) {
                        absentH++;
                    } else if ("У".equals(status)) {
                        absentU++;
                    } else if ("Б".equals(status)) {
                        absentB++;
                    }
                }
            }

            long totalAbsences = absentH + absentU + absentB;
            studentData.put("totalAbsences", totalAbsences);
            studentData.put("absentTypeH", absentH);
            studentData.put("absentTypeU", absentU);
            studentData.put("absentTypeB", absentB);

            studentsData.add(studentData);
        }

        // Сортируем учеников по фамилии
        studentsData.sort(Comparator.comparing(s -> (String) s.get("fullName")));

        // Добавляем атрибуты в модель
        model.addAttribute("students", studentsData);
        model.addAttribute("availableClasses", availableClasses);
        model.addAttribute("availableSubjects", availableSubjects);
        model.addAttribute("availableQuarters", Arrays.asList("I", "II", "III", "IV", "Итоговые оценки"));
        model.addAttribute("selectedClassId", selectedClassId);
        model.addAttribute("selectedSubjectId", selectedSubjectId);
        model.addAttribute("selectedQuarter", quarter);
        model.addAttribute("selectedClassName", availableClasses.get(selectedClassId));
        model.addAttribute("selectedSubjectName", availableSubjects.get(selectedSubjectId));
        model.addAttribute("isHomeroomTeacher", isHomeroomTeacher);
        model.addAttribute("isDirectorOrAdmin", isDirectorOrAdmin);
        model.addAttribute("content", "grades/teacher-view");

        System.out.println("=== TEACHER VIEW BUILT SUCCESSFULLY ===");
        System.out.println("Students count: " + studentsData.size());
        System.out.println("Available classes: " + availableClasses);
        System.out.println("Available subjects: " + availableSubjects);

        return "layout";
    }

    private LocalDateTime[] getQuarterDates(String quarter) {
        int year = 2025;
        LocalDateTime startDate, endDate;

        switch (quarter) {
            case "I":
                startDate = LocalDate.of(2025, Month.SEPTEMBER, 1).atStartOfDay();
                endDate = LocalDate.of(2025, Month.OCTOBER, 31).atTime(23, 59, 59);
                break;
            case "II":
                startDate = LocalDate.of(2025, Month.NOVEMBER, 1).atStartOfDay();
                endDate = LocalDate.of(2025, Month.DECEMBER, 31).atTime(23, 59, 59);
                break;
            case "III":
                startDate = LocalDate.of(2026, Month.JANUARY, 1).atStartOfDay();
                endDate = LocalDate.of(2026, Month.MARCH, 31).atTime(23, 59, 59);
                break;
            case "IV":
                startDate = LocalDate.of(2026, Month.APRIL, 1).atStartOfDay();
                endDate = LocalDate.of(2026, Month.MAY, 31).atTime(23, 59, 59);
                break;
            default:
                startDate = LocalDate.of(2025, Month.SEPTEMBER, 1).atStartOfDay();
                endDate = LocalDate.of(2026, Month.MAY, 31).atTime(23, 59, 59);
        }

        return new LocalDateTime[]{startDate, endDate};
    }
}