package com.school.portal.controller;

import com.school.portal.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    // Заглушки данных для демонстрации
    private List<User> users = new ArrayList<>();
    private List<SchoolClass> classes = new ArrayList<>();
    private List<Message> messages = new ArrayList<>();

    public ProfileController() {
        initializeDemoData();
    }

    private void initializeDemoData() {
        // Инициализация пользователей
        users.add(new User(1, "teacher", "Алексей", "Иванов", "Петрович", "Учитель", 1,
                "teacher@school.ru", "+79991234567", LocalDate.of(1980, 5, 15),
                "Классный руководитель 9А, преподаватель математики"));

        users.add(new User(2, "director", "Мария", "Петрова", "Сергеевна", "Директор", 2,
                "director@school.ru", "+79997654321", LocalDate.of(1975, 3, 22),
                "Директор школы, кандидат педагогических наук"));

        users.add(new User(3, "student1", "Дмитрий", "Сидоров", "Иванович", "Ученик", 3,
                "student1@school.ru", null, LocalDate.of(2007, 8, 10),
                "Успевающий ученик, участник олимпиад"));

        users.add(new User(4, "student2", "Анна", "Кузнецова", "Владимировна", "Ученик", 3,
                "student2@school.ru", "+79998887766", LocalDate.of(2008, 2, 25),
                "Отличница, занимается музыкой"));

        users.add(new User(5, "parent1", "Павел", "Смирнов", "Александрович", "Родитель", 4,
                "parent@mail.ru", "+79995554433", LocalDate.of(1982, 11, 5),
                "Председатель родительского комитета"));

        // Инициализация классов
        classes.add(new SchoolClass(1, 9, "А", 1)); // Классный руководитель - учитель с ID 1
        classes.add(new SchoolClass(2, 9, "Б", 6)); // Классный руководитель - другой учитель
        classes.add(new SchoolClass(3, 10, "А", 1));

        // Инициализация сообщений (для отправки директору)
        LocalDateTime now = LocalDateTime.now();
        messages.add(new Message(1, 1, 2, "Здравствуйте! Хотел уточнить по расписанию",
                now.minusDays(2), MessageStatus.READ));
        messages.add(new Message(2, 3, 2, "Вопрос по учебникам",
                now.minusDays(1), MessageStatus.NEW));
        messages.add(new Message(3, 5, 2, "Предложение по школьной форме",
                now.minusHours(5), MessageStatus.NEW));
    }

    // GET: /profile - главная страница профиля
    @GetMapping("/index")
    public String index(Model model) {
        int currentUserId = getCurrentUserId();
        User currentUser = findUserById(currentUserId);

        if (currentUser == null) {
            model.addAttribute("errorMessage", "Пользователь не найден");
            return "error";
        }

        // Создание модели представления
        ProfileViewModel profileModel = new ProfileViewModel();
        profileModel.setUserId(currentUser.getUserId());
        profileModel.setLastName(currentUser.getLastName());
        profileModel.setFirstName(currentUser.getFirstName());
        profileModel.setMiddleName(currentUser.getMiddleName());
        profileModel.setLogin(currentUser.getUsername());
        profileModel.setEmail(currentUser.getEmail());
        profileModel.setPhone(currentUser.getPhone());
        profileModel.setBirthDate(currentUser.getBirthDate());
        profileModel.setRoleName(currentUser.getRole());
        profileModel.setInfo(currentUser.getInfo());

        // Логика для динамических полей
        String homeroomTeacherName = null;

        if ("Ученик".equals(currentUser.getRole())) {
            // Для ученика: находим его класс
            SchoolClass studentClass = findClassByStudentId(currentUser.getUserId());
            if (studentClass != null) {
                profileModel.setClassInfo(studentClass.getClassName());

                // Находим классного руководителя
                User classTeacher = findUserById(studentClass.getClassTeacherId());
                if (classTeacher != null) {
                    homeroomTeacherName = classTeacher.getFullName();
                }
            } else {
                profileModel.setClassInfo("Не определен");
            }
        } else if ("Учитель".equals(currentUser.getRole())) {
            // Для учителя: проверяем, есть ли классное руководство
            SchoolClass teacherClass = findClassByTeacherId(currentUser.getUserId());
            profileModel.setClassInfo(teacherClass != null ?
                    teacherClass.getClassName() : "Нет классного руководства");
        } else if ("Родитель".equals(currentUser.getRole())) {
            // Для родителя: ищем связанного ученика
            User student = findStudentForParent(currentUser.getUserId());
            if (student != null) {
                profileModel.setStudentInfo(student.getFullName());

                // Находим класс ученика и классного руководителя
                SchoolClass studentClass = findClassByStudentId(student.getUserId());
                if (studentClass != null) {
                    User classTeacher = findUserById(studentClass.getClassTeacherId());
                    if (classTeacher != null) {
                        homeroomTeacherName = classTeacher.getFullName();
                    }
                }
            } else {
                profileModel.setStudentInfo("Нет привязанного ученика");
            }
        }

        // Получаем ID директора
        User director = findUserByRole("Директор");
        boolean isDirector = "Директор".equals(currentUser.getRole());

        model.addAttribute("viewModel", profileModel);
        model.addAttribute("homeroomTeacher", homeroomTeacherName);
        model.addAttribute("directorId", director != null ? director.getUserId() : null);
        model.addAttribute("isDirector", isDirector);
        model.addAttribute("title", "Мой профиль");
        model.addAttribute("activePage", "profile");
        model.addAttribute("content", "profile/index");

        return "layout";
    }

    // POST: /profile/send-message-to-director - отправка сообщения директору
    @PostMapping("/send-message-to-director")
    @ResponseBody
    public Map<String, Object> sendMessageToDirector(@RequestParam String body) {
        Map<String, Object> response = new HashMap<>();

        try {
            User director = findUserByRole("Директор");
            if (director == null) {
                response.put("success", false);
                response.put("message", "Директор не найден в системе.");
                return response;
            }

            if (body == null || body.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Сообщение не может быть пустым.");
                return response;
            }

            int currentUserId = getCurrentUserId();

            // Создание нового сообщения
            Message newMessage = new Message();
            newMessage.setMessageId(messages.size() + 1);
            newMessage.setFromUserId(currentUserId);
            newMessage.setToUserId(director.getUserId());
            newMessage.setMessageText(body.trim());
            newMessage.setSentAt(LocalDateTime.now());
            newMessage.setStatus(MessageStatus.NEW);

            messages.add(newMessage);

            response.put("success", true);
            response.put("message", "Сообщение директору успешно отправлено.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Произошла ошибка при отправке сообщения: " + e.getMessage());
        }

        return response;
    }

    // Вспомогательные методы
    private int getCurrentUserId() {
        // Для демо: возвращаем ID учителя (1)
        // В реальном приложении получаем из аутентификации
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Находим пользователя по username
        User currentUser = users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(users.get(0)); // По умолчанию возвращаем учителя

        return currentUser.getUserId();
    }

    private User findUserById(int userId) {
        return users.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst()
                .orElse(null);
    }

    private User findUserByRole(String role) {
        return users.stream()
                .filter(u -> role.equals(u.getRole()))
                .findFirst()
                .orElse(null);
    }

    private SchoolClass findClassByStudentId(int studentId) {
        // Для демо: ученик с ID 3 в классе 9А, ученик с ID 4 в классе 9Б
        if (studentId == 3) {
            return classes.get(0); // 9А
        } else if (studentId == 4) {
            return classes.get(1); // 9Б
        }
        return null;
    }

    private SchoolClass findClassByTeacherId(int teacherId) {
        return classes.stream()
                .filter(c -> c.getClassTeacherId() == teacherId)
                .findFirst()
                .orElse(null);
    }

    private User findStudentForParent(int parentId) {
        // Для демо: родитель с ID 5 связан с учеником ID 3
        if (parentId == 5) {
            return findUserById(3);
        }
        return null;
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

    @PostMapping("/submit-homework")
    @ResponseBody
    public Map<String, Object> submitHomework(
            @RequestParam Integer lessonId,
            @RequestParam String studentAnswer) {

        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String role = getCurrentUserRole(auth);

            // Проверка роли ученика
            if (!"ROLE_STUDENT".equals(role)) {
                response.put("success", false);
                response.put("message", "Только ученики могут отправлять домашнее задание.");
                return response;
            }

            if (lessonId == null || lessonId == 0) {
                response.put("success", false);
                response.put("message", "Не удалось определить урок.");
                return response;
            }

            String safeAnswer = studentAnswer != null ? studentAnswer.trim() : "";
            if (safeAnswer.isEmpty()) {
                response.put("success", false);
                response.put("message", "Пожалуйста, введите текст ответа.");
                return response;
            }

            // Здесь добавьте логику сохранения домашнего задания в вашей базе данных
            System.out.println("Сохранение ДЗ для урока " + lessonId + ": " + safeAnswer);

            response.put("success", true);
            response.put("message", "Домашнее задание успешно отправлено!");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Непредвиденная ошибка сервера: " + e.getMessage());
        }

        return response;
    }
}