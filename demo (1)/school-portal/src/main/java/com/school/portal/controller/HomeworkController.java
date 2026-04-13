package com.school.portal.controller;

import com.school.portal.model.*;
import com.school.portal.model.dto.HomeworkReviewItem;
import com.school.portal.model.dto.HomeworkReviewRequest;
import com.school.portal.model.dto.HomeworkReviewViewModel;
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

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ClassSubjectTeacherRepository classSubjectTeacherRepository;

    @GetMapping("/review")
    public String review(
            @RequestParam(value = "classId", required = false) Integer classId,
            @RequestParam(value = "subjectId", required = false) Integer subjectId,
            @RequestParam(value = "tab", required = false, defaultValue = "pending") String tab,
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

        // Получаем классы, которые ведет учитель
        Map<Integer, String> availableClasses = new LinkedHashMap<>();

        if (role.equals("ROLE_DIRECTOR")) {
            List<SchoolClass> allClasses = classRepository.findAll();
            for (SchoolClass sc : allClasses) {
                availableClasses.put(sc.getClassId(), sc.getClassName());
            }
        } else {
            List<Schedule> teacherLessons = scheduleRepository.findByTeacher_UserIdOrderByLessonDateTime(currentUser.getUserId());
            for (Schedule lesson : teacherLessons) {
                if (lesson.getSchoolClass() != null) {
                    availableClasses.put(lesson.getSchoolClass().getClassId(), lesson.getSchoolClass().getClassName());
                }
            }
        }

        if (availableClasses.isEmpty()) {
            HomeworkReviewViewModel viewModel = new HomeworkReviewViewModel();
            viewModel.setPendingSubmissions(new ArrayList<>());
            viewModel.setReviewedSubmissions(new ArrayList<>());
            viewModel.setAvailableClasses(new HashMap<>());
            viewModel.setAvailableSubjects(new HashMap<>());
            viewModel.setErrorMessage("У вас нет привязанных классов для проверки домашнего задания.");
            model.addAttribute("viewModel", viewModel);
            model.addAttribute("content", "homework/review");
            return "layout";
        }

        int selectedClassId = classId != null && availableClasses.containsKey(classId) ?
                classId : availableClasses.keySet().iterator().next();

        // Получаем предметы для выбранного класса
        Map<Integer, String> availableSubjects = new LinkedHashMap<>();

        if (role.equals("ROLE_DIRECTOR")) {
            List<Subject> allSubjects = subjectRepository.findAll();
            for (Subject subj : allSubjects) {
                availableSubjects.put(subj.getSubjectId(), subj.getSubjectName());
            }
        } else {
            LocalDateTime startDate = LocalDateTime.now().minusYears(1);
            LocalDateTime endDate = LocalDateTime.now().plusYears(1);
            List<Schedule> lessonsInClass = scheduleRepository.findLessonsForClassBetween(selectedClassId, startDate, endDate);
            for (Schedule lesson : lessonsInClass) {
                if (lesson.getTeacher() != null &&
                        lesson.getTeacher().getUserId().equals(currentUser.getUserId()) &&
                        lesson.getSubject() != null) {
                    availableSubjects.put(lesson.getSubject().getSubjectId(), lesson.getSubject().getSubjectName());
                }
            }
        }

        int selectedSubjectId = 0;
        if (subjectId != null && availableSubjects.containsKey(subjectId)) {
            selectedSubjectId = subjectId;
        } else if (!availableSubjects.isEmpty()) {
            selectedSubjectId = availableSubjects.keySet().iterator().next();
        }

        // Получаем учеников класса
        List<User> students = studentClassRepository.findStudentsByClassId(selectedClassId);

        // Получаем все ДЗ и разделяем по статусам
        List<HomeworkReviewItem> pendingItems = new ArrayList<>();
        List<HomeworkReviewItem> reviewedItems = new ArrayList<>();

        for (User student : students) {
            List<Homework> studentHomeworks = homeworkRepository.findByStudentUserId(student.getUserId());

            for (Homework homework : studentHomeworks) {
                Schedule lesson = homework.getLesson();
                if (lesson == null) continue;

                if (lesson.getSchoolClass() == null || !lesson.getSchoolClass().getClassId().equals(selectedClassId)) {
                    continue;
                }

                boolean canReview = false;
                if (role.equals("ROLE_DIRECTOR")) {
                    canReview = true;
                } else if (lesson.getTeacher() != null && lesson.getTeacher().getUserId().equals(currentUser.getUserId())) {
                    canReview = true;
                }

                if (!canReview) continue;

                if (selectedSubjectId > 0 && lesson.getSubject() != null &&
                        !lesson.getSubject().getSubjectId().equals(selectedSubjectId)) {
                    continue;
                }

                HomeworkReviewItem item = convertToReviewItem(homework);
                if (item != null) {
                    // Разделяем по статусу: 1 - Сдано (на проверку), 2 - Проверено
                    if (homework.getStatus() == 1) {
                        pendingItems.add(item);
                    } else if (homework.getStatus() == 2) {
                        reviewedItems.add(item);
                    }
                }
            }
        }

        // Сортируем по дате сдачи (новые сверху)
        pendingItems.sort(Comparator.comparing(HomeworkReviewItem::getSubmissionDate, Comparator.nullsLast(Comparator.reverseOrder())));
        reviewedItems.sort(Comparator.comparing(HomeworkReviewItem::getSubmissionDate, Comparator.nullsLast(Comparator.reverseOrder())));

        HomeworkReviewViewModel viewModel = new HomeworkReviewViewModel();
        viewModel.setPendingSubmissions(pendingItems);
        viewModel.setReviewedSubmissions(reviewedItems);
        viewModel.setAvailableClasses(availableClasses);
        viewModel.setAvailableSubjects(availableSubjects);
        viewModel.setSelectedClassId(selectedClassId);
        viewModel.setSelectedSubjectId(selectedSubjectId);
        viewModel.setSelectedClassName(availableClasses.get(selectedClassId));
        viewModel.setSelectedSubjectName(selectedSubjectId > 0 ? availableSubjects.get(selectedSubjectId) : "Все предметы");
        viewModel.setActiveTab(tab);

        model.addAttribute("viewModel", viewModel);
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

            homework.setStatus(2);
            homework.setTeacherComment(request.getComment());
            homeworkRepository.save(homework);

            if (request.getGradeValue() != null && request.getGradeValue() > 0) {
                Optional<Grade> existingGrade = homework.getGrades().stream().findFirst();

                Grade grade;
                if (existingGrade.isPresent()) {
                    grade = existingGrade.get();
                    grade.setGradeValue(request.getGradeValue());
                    grade.setComment(request.getComment());
                } else {
                    grade = new Grade();
                    grade.setStudent(homework.getStudent());
                    grade.setLesson(homework.getLesson());
                    grade.setHomework(homework);
                    grade.setGradeValue(request.getGradeValue());
                    grade.setComment(request.getComment());
                    grade.setDate(LocalDateTime.now());
                }

                gradeRepository.save(grade);
                response.put("gradeId", grade.getGradeId());
            } else if (request.getGradeValue() == null && request.getExistingGradeId() != null) {
                gradeRepository.deleteById(request.getExistingGradeId());
            }

            response.put("success", true);
            response.put("message", "Оценка и комментарий сохранены.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка сохранения: " + e.getMessage());
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
            Schedule lesson = homework.getLesson();

            item.setLessonDate(lesson.getLessonDateTime());

            if (lesson.getLessonDateTime() != null) {
                int lessonNumber = getLessonNumberByTime(lesson.getLessonDateTime().toLocalTime());
                item.setLessonNumber(lessonNumber);
            }

            if (lesson.getSubject() != null) {
                item.setSubjectId(lesson.getSubject().getSubjectId());
                item.setSubjectName(lesson.getSubject().getSubjectName());
            }

            item.setLessonTopic(lesson.getLessonTopic());
            item.setHomeworkText(lesson.getHomeworkText());

            if (lesson.getSchoolClass() != null) {
                item.setClassId(lesson.getSchoolClass().getClassId());
                item.setClassName(lesson.getSchoolClass().getClassName());
            }
        }

        item.setSubmissionDate(homework.getDate());
        item.setStudentAnswer(homework.getText());
        item.setStatusId(homework.getStatus());
        item.setCurrentTeacherComment(homework.getTeacherComment());

        if (homework.getGrades() != null && !homework.getGrades().isEmpty()) {
            Grade grade = homework.getGrades().iterator().next();
            if (grade != null) {
                item.setGradeId(grade.getGradeId());
                item.setCurrentGradeValue(grade.getGradeValue());
                item.setCurrentGradeComment(grade.getComment());
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
    @GetMapping("/get-student-answer")
    @ResponseBody
    public Map<String, Object> getStudentAnswer(@RequestParam Integer homeworkId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Homework homework = homeworkRepository.findById(homeworkId)
                    .orElseThrow(() -> new RuntimeException("Задание не найдено"));

            response.put("success", true);
            response.put("answer", homework.getText());
        } catch (Exception e) {
            response.put("success", false);
            response.put("answer", "Ошибка: " + e.getMessage());
        }

        return response;
    }
}