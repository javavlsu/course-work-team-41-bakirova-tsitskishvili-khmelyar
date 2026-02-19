package com.school.portal.controller;

import com.school.portal.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/homework")
public class HomeworkController {

    // Заглушка данных для демонстрации
    private Map<Integer, List<HomeworkReviewItem>> homeworkData = new HashMap<>();

    public HomeworkController() {
        initializeDemoData();
    }

    private void initializeDemoData() {
        List<HomeworkReviewItem> class1Homework = new ArrayList<>();

        // Домашние задания для класса 1 (9 "А")
        for (int i = 1; i <= 8; i++) {
            HomeworkReviewItem item = new HomeworkReviewItem();
            item.setHomeworkId(i);
            item.setStudentId(i);
            item.setStudentFullName(getRandomStudentName(i));
            item.setClassId(1);
            item.setClassName("9 \"А\"");
            item.setSubjectName(getRandomSubject(i));
            item.setLessonDate(LocalDateTime.now().minusDays(i));
            item.setLessonNumber(i);
            item.setSubmissionDate(LocalDateTime.now().minusHours(i));
            item.setStudentAnswer(getRandomAnswer(i));
            item.setStatusId(i % 3); // 0, 1, 2
            item.setCurrentTeacherComment(getRandomAssignment(i));

            // Некоторые задания имеют оценки
            if (i % 2 == 0) {
                item.setGradeId(i * 10);
                item.setCurrentGradeValue(getRandomGrade(i));
            }

            class1Homework.add(item);
        }

        // Домашние задания для класса 2 (9 "Б")
        List<HomeworkReviewItem> class2Homework = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            HomeworkReviewItem item = new HomeworkReviewItem();
            item.setHomeworkId(i + 100);
            item.setStudentId(i + 100);
            item.setStudentFullName(getRandomStudentName(i + 100));
            item.setClassId(2);
            item.setClassName("9 \"Б\"");
            item.setSubjectName(getRandomSubject(i + 100));
            item.setLessonDate(LocalDateTime.now().minusDays(i + 5));
            item.setLessonNumber(i);
            item.setSubmissionDate(LocalDateTime.now().minusHours(i + 5));
            item.setStudentAnswer(getRandomAnswer(i + 100));
            item.setStatusId((i + 1) % 3);
            item.setCurrentTeacherComment(getRandomAssignment(i + 100));

            if (i % 3 == 0) {
                item.setGradeId((i + 100) * 10);
                item.setCurrentGradeValue(getRandomGrade(i + 100));
            }

            class2Homework.add(item);
        }

        homeworkData.put(1, class1Homework);
        homeworkData.put(2, class2Homework);
    }

    private String getRandomStudentName(int seed) {
        String[] names = {
                "Иванов Алексей Петрович",
                "Петрова Мария Сергеевна",
                "Сидоров Дмитрий Иванович",
                "Кузнецова Анна Владимировна",
                "Смирнов Павел Александрович",
                "Федорова Елена Дмитриевна",
                "Морозов Игорь Сергеевич",
                "Волкова Ольга Павловна",
                "Алексеев Николай Викторович",
                "Лебедева Татьяна Игоревна"
        };
        return names[seed % names.length];
    }

    private String getRandomSubject(int seed) {
        String[] subjects = {
                "Математика",
                "Русский язык",
                "Физика",
                "Химия",
                "История",
                "Биология",
                "Английский язык",
                "Информатика"
        };
        return subjects[seed % subjects.length];
    }

    private String getRandomAnswer(int seed) {
        String[] answers = {
                "Решил задачу методом подстановки. Ответ: x = 5.",
                "Написал сочинение на тему 'Мое лето'. Приложил файл с текстом.",
                "Выполнил лабораторную работу по физике. Измерил ускорение свободного падения.",
                "Подготовил доклад по истории Древнего Рима. Основные тезисы: политическая система, культура, военные походы.",
                "Решил все примеры из учебника. Самым сложным был пример №15 с логарифмами.",
                "Выполнил упражнения по английскому языку. Перевел текст о современных технологиях.",
                "Написал программу на Java для вычисления факториала. Код прилагается.",
                "Подготовил презентацию по биологии на тему 'Клеточное строение организмов'."
        };
        return answers[seed % answers.length];
    }

    private String getRandomAssignment(int seed) {
        String[] assignments = {
                "Решить задачи №1-10 из учебника, страница 45.",
                "Написать сочинение на тему 'Мое любимое время года' (объем: 1-2 страницы).",
                "Подготовить доклад по теме 'Законы Ньютона' с примерами из жизни.",
                "Выполнить лабораторную работу №3 'Химические реакции'.",
                "Изучить параграф 15, ответить на вопросы в конце параграфа.",
                "Подготовить презентацию по теме 'Эволюционная теория Дарвина'.",
                "Выучить слова по теме 'Travel', составить 10 предложений.",
                "Написать программу для решения квадратного уравнения на языке Java."
        };
        return assignments[seed % assignments.length];
    }

    private Integer getRandomGrade(int seed) {
        int[] grades = {2, 3, 4, 5};
        return grades[seed % grades.length];
    }

    @GetMapping("/review")
    public String review(
            @RequestParam(value = "classId", required = false) Integer classId,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String role = getCurrentUserRole(auth);

        // Проверка роли - только учитель и директор
        if (!role.equals("ROLE_TEACHER") && !role.equals("ROLE_DIRECTOR")) {
            model.addAttribute("errorMessage", "Доступ запрещен. Только для учителей и директоров.");
            return "error";
        }

        model.addAttribute("title", "Проверка ДЗ");
        model.addAttribute("activePage", "homework");

        // Доступные классы (заглушка)
        Map<Integer, String> availableClasses = new LinkedHashMap<>();
        availableClasses.put(1, "9 \"А\"");
        availableClasses.put(2, "9 \"Б\"");
        availableClasses.put(3, "10 \"А\"");
        availableClasses.put(4, "10 \"Б\"");

        if (!availableClasses.isEmpty()) {
            int selectedClassId = classId != null ? classId : availableClasses.keySet().iterator().next();
            String selectedClassName = availableClasses.get(selectedClassId);

            // Получаем данные для выбранного класса
            List<HomeworkReviewItem> submissions = homeworkData.getOrDefault(selectedClassId, new ArrayList<>());

            // Сортируем по дате сдачи (новые первыми)
            submissions.sort(Comparator.comparing(HomeworkReviewItem::getSubmissionDate).reversed());

            HomeworkReviewViewModel viewModel = new HomeworkReviewViewModel();
            viewModel.setSubmissions(submissions);
            viewModel.setAvailableClasses(availableClasses);
            viewModel.setSelectedClassId(selectedClassId);
            viewModel.setSelectedClassName(selectedClassName);

            model.addAttribute("viewModel", viewModel);
            model.addAttribute("content", "homework/review");
        } else {
            HomeworkReviewViewModel viewModel = new HomeworkReviewViewModel();
            viewModel.setSubmissions(new ArrayList<>());
            viewModel.setAvailableClasses(new HashMap<>());
            viewModel.setErrorMessage("Нет доступных классов для проверки домашнего задания.");

            model.addAttribute("viewModel", viewModel);
            model.addAttribute("content", "homework/review");
        }

        return "layout";
    }

    @PostMapping("/save-review")
    @ResponseBody
    public Map<String, Object> saveReview(@RequestBody HomeworkReviewRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Симулируем сохранение в базе данных
            if (request.getHomeworkId() > 0) {
                // Находим задание во всех классах
                for (List<HomeworkReviewItem> classHomework : homeworkData.values()) {
                    for (HomeworkReviewItem item : classHomework) {
                        if (item.getHomeworkId() == request.getHomeworkId()) {
                            // Обновляем статус
                            item.setStatusId(2); // Проверено

                            // Обновляем оценку
                            item.setCurrentGradeValue(request.getGradeValue());

                            // Если gradeId был null, создаем новый
                            if (item.getGradeId() == null) {
                                item.setGradeId(request.getHomeworkId() * 100);
                            }

                            response.put("success", true);
                            response.put("message", "Оценка и комментарий сохранены.");
                            response.put("newGradeId", item.getGradeId());
                            return response;
                        }
                    }
                }

                response.put("success", false);
                response.put("message", "Задание не найдено.");
            } else {
                response.put("success", false);
                response.put("message", "Некорректный ID задания.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка сохранения: " + e.getMessage());
        }

        return response;
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