package com.school.portal.controller;

import com.school.portal.model.*;
import com.school.portal.model.dto.*;
import com.school.portal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/homework")
public class HomeworkController {

    @Autowired
    private HomeworkRepository homeworkRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchoolClassRepository classRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private StudentClassRepository studentClassRepository;

    @GetMapping("/review")
    public String review(
            @RequestParam(value = "classId", required = false) Integer classId,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        String role = getCurrentUserRole(auth);

        // Проверка роли - только учитель и директор
        if (!role.equals("ROLE_TEACHER") && !role.equals("ROLE_DIRECTOR")) {
            model.addAttribute("errorMessage", "Доступ запрещен. Только для учителей и директоров.");
            return "error";
        }

        model.addAttribute("title", "Проверка ДЗ");
        model.addAttribute("activePage", "homework");

        // Доступные классы для учителя
        List<SchoolClass> teacherClasses;
        if (role.equals("ROLE_DIRECTOR")) {
            teacherClasses = classRepository.findAll();
        } else {
            teacherClasses = classRepository.findClassesByTeacherId(currentUser.getUserId());
        }

        Map<Integer, String> availableClasses = new LinkedHashMap<>();
        for (SchoolClass sc : teacherClasses) {
            availableClasses.put(sc.getClassId(), sc.getClassName());
        }

        if (!availableClasses.isEmpty()) {
            int selectedClassId = classId != null && availableClasses.containsKey(classId) ?
                    classId : availableClasses.keySet().iterator().next();

            // Получаем учеников класса
            List<StudentClass> studentClasses = studentClassRepository.findBySchoolClassClassId(selectedClassId);
            Set<Integer> studentIds = studentClasses.stream()
                    .map(sc -> sc.getStudent().getUserId())
                    .collect(Collectors.toSet());

            // Получаем домашние задания для учеников этого класса
            List<Homework> submissions = new ArrayList<>();
            for (Integer studentId : studentIds) {
                submissions.addAll(homeworkRepository.findByStudentUserId(studentId));
            }

            // Преобразуем в ViewModel
            List<HomeworkReviewItem> reviewItems = submissions.stream()
                    .map(this::convertToReviewItem)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(HomeworkReviewItem::getSubmissionDate).reversed())
                    .collect(Collectors.toList());

            HomeworkReviewViewModel viewModel = new HomeworkReviewViewModel();
            viewModel.setSubmissions(reviewItems);
            viewModel.setAvailableClasses(availableClasses);
            viewModel.setSelectedClassId(selectedClassId);
            viewModel.setSelectedClassName(availableClasses.get(selectedClassId));

            model.addAttribute("viewModel", viewModel);
        } else {
            HomeworkReviewViewModel viewModel = new HomeworkReviewViewModel();
            viewModel.setSubmissions(new ArrayList<>());
            viewModel.setAvailableClasses(new HashMap<>());
            viewModel.setErrorMessage("Нет доступных классов для проверки домашнего задания.");

            model.addAttribute("viewModel", viewModel);
        }

        model.addAttribute("content", "homework/review");
        return "layout";
    }

    @PostMapping("/save-review")
    @ResponseBody
    public Map<String, Object> saveReview(@RequestBody HomeworkReviewRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Homework homework = homeworkRepository.findById(request.getHomeworkId())
                    .orElseThrow(() -> new RuntimeException("Задание не найдено"));

            homework.setStatus(2); // Проверено
            homework.setTeacherComment(request.getComment());
            homeworkRepository.save(homework);

            // Если есть оценка, создаем или обновляем
            if (request.getGradeValue() != null) {
                // Проверяем, есть ли уже оценка за это ДЗ
                Optional<Grade> existingGrade = homework.getGrades().stream().findFirst();

                Grade grade;
                if (existingGrade.isPresent()) {
                    grade = existingGrade.get();
                } else {
                    grade = new Grade();
                    grade.setStudent(homework.getStudent());
                    grade.setLesson(homework.getLesson());
                    grade.setHomework(homework);
                    grade.setDate(LocalDateTime.now());
                }

                grade.setGradeValue(request.getGradeValue());
                grade.setComment(request.getComment());
                gradeRepository.save(grade);
            }

            response.put("success", true);
            response.put("message", "Оценка и комментарий сохранены.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка сохранения: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submitHomework(
            @RequestParam Integer lessonId,
            @RequestParam String studentAnswer) {

        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User student = userRepository.findByLogin(username)
                    .orElseThrow(() -> new RuntimeException("Ученик не найден"));

            Schedule lesson = scheduleRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Урок не найден"));

            // Проверяем, не отправлял ли уже ученик ДЗ за этот урок
            Optional<Homework> existingHomework = homeworkRepository.findByStudentUserId(student.getUserId())
                    .stream()
                    .filter(h -> h.getLesson() != null && h.getLesson().getLessonId() == lessonId)
                    .findFirst();

            Homework homework;
            if (existingHomework.isPresent()) {
                homework = existingHomework.get();
                homework.setText(studentAnswer);
                homework.setStatus(1); // Сдано
                homework.setDate(LocalDateTime.now());
            } else {
                homework = new Homework();
                homework.setStudent(student);
                homework.setLesson(lesson);
                homework.setText(studentAnswer);
                homework.setDate(LocalDateTime.now());
                homework.setStatus(1); // Сдано
            }

            homeworkRepository.save(homework);

            response.put("success", true);
            response.put("message", "Домашнее задание успешно отправлено!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }

        return response;
    }

    private HomeworkReviewItem convertToReviewItem(Homework homework) {
        if (homework == null || homework.getStudent() == null) {
            return null;
        }

        HomeworkReviewItem item = new HomeworkReviewItem();
        item.setHomeworkId(homework.getHomeworkId());
        item.setStudentId(homework.getStudent().getUserId());
        item.setStudentFullName(homework.getStudent().getFullName());

        if (homework.getLesson() != null) {
            // Устанавливаем дату урока
            item.setLessonDate(homework.getLesson().getLessonDateTime());

            // Определяем номер урока по времени
            if (homework.getLesson().getLessonDateTime() != null) {
                int lessonNumber = getLessonNumberByTime(homework.getLesson().getLessonDateTime().toLocalTime());
                item.setLessonNumber(lessonNumber);
            }

            // Предмет
            if (homework.getLesson().getSubject() != null) {
                item.setSubjectName(homework.getLesson().getSubject().getSubjectName());
            }

            // Класс
            if (homework.getLesson().getSchoolClass() != null) {
                item.setClassId(homework.getLesson().getSchoolClass().getClassId());
                item.setClassName(homework.getLesson().getSchoolClass().getClassName());
            }
        }

        item.setSubmissionDate(homework.getDate());
        item.setStudentAnswer(homework.getText());
        item.setStatusId(homework.getStatus());
        item.setCurrentTeacherComment(homework.getTeacherComment());

        // Проверяем наличие оценки
        if (homework.getGrades() != null && !homework.getGrades().isEmpty()) {
            Grade grade = homework.getGrades().iterator().next();
            if (grade != null) {
                item.setGradeId(grade.getGradeId());
                item.setCurrentGradeValue(grade.getGradeValue());
            }
        }

        return item;
    }

    private int getLessonNumberByTime(LocalTime time) {
        if (time == null) return 1;
        if (time.isBefore(LocalTime.of(9, 30))) return 1;
        if (time.isBefore(LocalTime.of(11, 0))) return 2;
        if (time.isBefore(LocalTime.of(12, 30))) return 3;
        if (time.isBefore(LocalTime.of(14, 30))) return 4;
        if (time.isBefore(LocalTime.of(16, 0))) return 5;
        return 6;
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