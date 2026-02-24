package com.school.portal.controller;

import com.school.portal.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/messages")
public class MessagesController {

    // Заглушки данных для демонстрации
    private List<Message> messages = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<String> roles = new ArrayList<>();

    public MessagesController() {
        initializeDemoData();
    }

    private void initializeDemoData() {
        // Инициализация ролей
        roles.add("Учитель");
        roles.add("Директор");
        roles.add("Ученик");
        roles.add("Родитель");

        // Инициализация пользователей
        users.add(createMockUser(1, "teacher1", "Алексей", "Иванов", "Петрович", "Учитель"));
        users.add(createMockUser(2, "director", "Мария", "Петрова", "Сергеевна", "Директор"));
        users.add(createMockUser(3, "student1", "Дмитрий", "Сидоров", "Иванович", "Ученик"));
        users.add(createMockUser(4, "student2", "Анна", "Кузнецова", "Владимировна", "Ученик"));
        users.add(createMockUser(5, "parent1", "Павел", "Смирнов", "Александрович", "Родитель"));
        users.add(createMockUser(6, "teacher2", "Елена", "Федорова", "Дмитриевна", "Учитель"));

        // Инициализация сообщений
        LocalDateTime now = LocalDateTime.now();

        // Входящие сообщения для текущего пользователя (ID = 1)
        messages.add(new Message(1, 2, 1, "Привет! Напоминаю о собрании завтра в 18:00",
                now.minusHours(2), MessageStatus.NEW));
        messages.add(new Message(2, 3, 1, "Здравствуйте! Можно получить консультацию?",
                now.minusDays(1), MessageStatus.READ));
        messages.add(new Message(3, 4, 1, "Спасибо за помощь с домашним заданием!",
                now.minusDays(2), MessageStatus.READ));
        messages.add(new Message(4, 5, 1, "Добрый день! Хотел уточнить про успеваемость",
                now.minusDays(3), MessageStatus.ARCHIVED));

        // Отправленные сообщения от текущего пользователя
        messages.add(new Message(5, 1, 2, "Отчет по успеваемости готов",
                now.minusHours(1), MessageStatus.NEW));
        messages.add(new Message(6, 1, 3, "Консультация будет в пятницу",
                now.minusDays(1), MessageStatus.READ));
        messages.add(new Message(7, 1, 4, "Отличная работа на уроке!",
                now.minusDays(2), MessageStatus.READ));
        messages.add(new Message(8, 1, 5, "Приглашаю на родительское собрание",
                now.minusDays(3), MessageStatus.READ));
    }

    // Главная страница сообщений
    @GetMapping("/index")
    public String index(@RequestParam(value = "filter", defaultValue = "inbox") String filter,
                        Model model) {

        int currentUserId = getCurrentUserId();
        List<Message> filteredMessages = new ArrayList<>();

        // Фильтрация сообщений
        if ("inbox".equals(filter)) {
            filteredMessages = messages.stream()
                    .filter(m -> m.getToUserId() == currentUserId &&
                            m.getStatus() != MessageStatus.ARCHIVED)
                    .sorted((m1, m2) -> {
                        // Сначала новые, потом прочитанные
                        if (m1.getStatus() == MessageStatus.NEW && m2.getStatus() != MessageStatus.NEW) {
                            return -1;
                        } else if (m1.getStatus() != MessageStatus.NEW && m2.getStatus() == MessageStatus.NEW) {
                            return 1;
                        }
                        // Затем по дате (новые сверху)
                        return m2.getSentAt().compareTo(m1.getSentAt());
                    })
                    .collect(Collectors.toList());
        } else if ("sent".equals(filter)) {
            filteredMessages = messages.stream()
                    .filter(m -> m.getFromUserId() == currentUserId)
                    .sorted((m1, m2) -> m2.getSentAt().compareTo(m1.getSentAt()))
                    .collect(Collectors.toList());
        } else if ("archive".equals(filter)) {
            filteredMessages = messages.stream()
                    .filter(m -> m.getToUserId() == currentUserId &&
                            m.getStatus() == MessageStatus.ARCHIVED)
                    .sorted((m1, m2) -> m2.getSentAt().compareTo(m1.getSentAt()))
                    .collect(Collectors.toList());
        }

        // Добавляем информацию об отправителях/получателях
        for (Message message : filteredMessages) {
            if (message.getFromUser() == null) {
                message.setFromUser(findUserById(message.getFromUserId()));
            }
            if (message.getToUser() == null) {
                message.setToUser(findUserById(message.getToUserId()));
            }
        }

        model.addAttribute("messages", filteredMessages);
        model.addAttribute("filter", filter);
        model.addAttribute("recipientRoles", roles);
        model.addAttribute("title", "Сообщения");
        model.addAttribute("activePage", "messages");
        model.addAttribute("content", "messages/index");

        return "layout";
    }

    // Поиск пользователей (AJAX)
    @GetMapping("/search-user")
    @ResponseBody
    public List<Map<String, Object>> searchUser(
            @RequestParam String fullName,
            @RequestParam String role) {

        List<Map<String, Object>> result = new ArrayList<>();

        // Фильтрация пользователей
        List<User> filteredUsers = users.stream()
                .filter(u -> u.getRole() != null && role.equals(u.getRole().getRoleName()))
                .filter(u -> {
                    String searchName = fullName.toLowerCase().trim();
                    String userFullName = u.getFullName().toLowerCase();
                    return userFullName.contains(searchName) ||
                            u.getLastName().toLowerCase().contains(searchName) ||
                            u.getFirstName().toLowerCase().contains(searchName);
                })
                .limit(10)
                .collect(Collectors.toList());

        // Формирование результата
        for (User user : filteredUsers) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", user.getUserId());
            userMap.put("fullName", user.getFullName());
            result.add(userMap);
        }

        return result;
    }

    // Отправка нового сообщения
    @PostMapping("/create")
    public String createMessage(@ModelAttribute SendMessageViewModel model) {
        try {
            int currentUserId = getCurrentUserId();

            // Создание нового сообщения
            Message newMessage = new Message();
            newMessage.setMessageId(messages.size() + 1);
            newMessage.setFromUserId(currentUserId);
            newMessage.setToUserId(model.getRecipientId());
            newMessage.setMessageText(model.getBody());
            newMessage.setSentAt(LocalDateTime.now());
            newMessage.setStatus(MessageStatus.NEW);

            messages.add(newMessage);

            return "redirect:/messages/index?filter=sent&success=true";
        } catch (Exception e) {
            return "redirect:/messages/index?filter=sent&error=true";
        }
    }

    // Пометить как прочитанное
    @PostMapping("/mark-as-read")
    public String markAsRead(@RequestParam int id) {
        Message message = findMessageById(id);
        if (message != null && message.getToUserId() == getCurrentUserId()) {
            message.setStatus(MessageStatus.READ);
        }
        return "redirect:/messages/index?filter=inbox";
    }

    // Архивировать сообщение
    @PostMapping("/archive")
    public String archiveMessage(@RequestParam int id) {
        Message message = findMessageById(id);
        if (message != null && message.getToUserId() == getCurrentUserId() &&
                message.getStatus() != MessageStatus.ARCHIVED) {
            message.setStatus(MessageStatus.ARCHIVED);
        }
        return "redirect:/messages/index?filter=inbox";
    }

    // Восстановить из архива
    @PostMapping("/restore")
    public String restoreMessage(@RequestParam int id) {
        Message message = findMessageById(id);
        if (message != null && message.getToUserId() == getCurrentUserId() &&
                message.getStatus() == MessageStatus.ARCHIVED) {
            message.setStatus(MessageStatus.READ);
        }
        return "redirect:/messages/index?filter=archive";
    }

    // Удалить сообщение (только отправленные со статусом NEW)
    @PostMapping("/delete")
    public String deleteMessage(@RequestParam int id) {
        Message message = findMessageById(id);
        if (message != null && message.getFromUserId() == getCurrentUserId() &&
                message.getStatus() == MessageStatus.NEW) {
            messages.remove(message);
        }
        return "redirect:/messages/index?filter=sent";
    }

    // Вспомогательные методы
    private int getCurrentUserId() {
        // В реальном приложении получаем ID из аутентификации
        // Для демо возвращаем ID учителя (1)
        return 1;
    }

    private User findUserById(int userId) {
        return users.stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst()
                .orElse(null);
    }

    private Message findMessageById(int messageId) {
        return messages.stream()
                .filter(m -> m.getMessageId() == messageId)
                .findFirst()
                .orElse(null);
    }

    // Вспомогательный метод для создания тестовых пользователей
    private User createMockUser(int id, String login, String first, String last, String middle, String roleName) {
        User u = new User();
        u.setUserId(id);
        u.setLogin(login);
        u.setFirstName(first);
        u.setLastName(last);
        u.setMiddleName(middle);
        u.setPassword("1234"); // Временный пароль для заглушки

        // Создаем объект Role, как того требует наша новая архитектура
        Role role = new Role();
        role.setRoleName(roleName);
        u.setRole(role);

        return u;
    }
}