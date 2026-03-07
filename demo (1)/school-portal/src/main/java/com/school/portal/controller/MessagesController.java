package com.school.portal.controller;

import com.school.portal.model.*;
import com.school.portal.model.dto.SendMessageViewModel;
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
        Message msg1 = new Message();
        msg1.setMessageId(1);
        msg1.setFromUser(findUserById(2));
        msg1.setToUser(findUserById(1));
        msg1.setMessageText("Привет! Напоминаю о собрании завтра в 18:00");
        msg1.setSentAt(now.minusHours(2));
        msg1.setStatus(0); // NEW
        messages.add(msg1);

        Message msg2 = new Message();
        msg2.setMessageId(2);
        msg2.setFromUser(findUserById(3));
        msg2.setToUser(findUserById(1));
        msg2.setMessageText("Здравствуйте! Можно получить консультацию?");
        msg2.setSentAt(now.minusDays(1));
        msg2.setStatus(1); // READ
        messages.add(msg2);

        Message msg3 = new Message();
        msg3.setMessageId(3);
        msg3.setFromUser(findUserById(4));
        msg3.setToUser(findUserById(1));
        msg3.setMessageText("Спасибо за помощь с домашним заданием!");
        msg3.setSentAt(now.minusDays(2));
        msg3.setStatus(1); // READ
        messages.add(msg3);

        Message msg4 = new Message();
        msg4.setMessageId(4);
        msg4.setFromUser(findUserById(5));
        msg4.setToUser(findUserById(1));
        msg4.setMessageText("Добрый день! Хотел уточнить про успеваемость");
        msg4.setSentAt(now.minusDays(3));
        msg4.setStatus(2); // ARCHIVED
        messages.add(msg4);

        // Отправленные сообщения от текущего пользователя
        Message msg5 = new Message();
        msg5.setMessageId(5);
        msg5.setFromUser(findUserById(1));
        msg5.setToUser(findUserById(2));
        msg5.setMessageText("Отчет по успеваемости готов");
        msg5.setSentAt(now.minusHours(1));
        msg5.setStatus(0); // NEW
        messages.add(msg5);

        Message msg6 = new Message();
        msg6.setMessageId(6);
        msg6.setFromUser(findUserById(1));
        msg6.setToUser(findUserById(3));
        msg6.setMessageText("Консультация будет в пятницу");
        msg6.setSentAt(now.minusDays(1));
        msg6.setStatus(1); // READ
        messages.add(msg6);

        Message msg7 = new Message();
        msg7.setMessageId(7);
        msg7.setFromUser(findUserById(1));
        msg7.setToUser(findUserById(4));
        msg7.setMessageText("Отличная работа на уроке!");
        msg7.setSentAt(now.minusDays(2));
        msg7.setStatus(1); // READ
        messages.add(msg7);

        Message msg8 = new Message();
        msg8.setMessageId(8);
        msg8.setFromUser(findUserById(1));
        msg8.setToUser(findUserById(5));
        msg8.setMessageText("Приглашаю на родительское собрание");
        msg8.setSentAt(now.minusDays(3));
        msg8.setStatus(1); // READ
        messages.add(msg8);
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
                    .filter(m -> m.getToUser() != null &&
                            m.getToUser().getUserId() == currentUserId &&
                            m.getStatus() != 2) // не ARCHIVED
                    .sorted((m1, m2) -> {
                        // Сначала новые, потом прочитанные
                        if (m1.getStatus() == 0 && m2.getStatus() != 0) {
                            return -1;
                        } else if (m1.getStatus() != 0 && m2.getStatus() == 0) {
                            return 1;
                        }
                        // Затем по дате (новые сверху)
                        return m2.getSentAt().compareTo(m1.getSentAt());
                    })
                    .collect(Collectors.toList());
        } else if ("sent".equals(filter)) {
            filteredMessages = messages.stream()
                    .filter(m -> m.getFromUser() != null &&
                            m.getFromUser().getUserId() == currentUserId)
                    .sorted((m1, m2) -> m2.getSentAt().compareTo(m1.getSentAt()))
                    .collect(Collectors.toList());
        } else if ("archive".equals(filter)) {
            filteredMessages = messages.stream()
                    .filter(m -> m.getToUser() != null &&
                            m.getToUser().getUserId() == currentUserId &&
                            m.getStatus() == 2) // ARCHIVED
                    .sorted((m1, m2) -> m2.getSentAt().compareTo(m1.getSentAt()))
                    .collect(Collectors.toList());
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
            User currentUser = findUserById(currentUserId);
            User recipient = findUserById(model.getRecipientId());

            if (currentUser == null || recipient == null) {
                return "redirect:/messages/index?filter=sent&error=true";
            }

            // Создание нового сообщения
            Message newMessage = new Message();
            newMessage.setMessageId(messages.size() + 1);
            newMessage.setFromUser(currentUser);
            newMessage.setToUser(recipient);
            newMessage.setMessageText(model.getBody());
            newMessage.setSentAt(LocalDateTime.now());
            newMessage.setStatus(0); // NEW

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
        if (message != null && message.getToUser() != null &&
                message.getToUser().getUserId() == getCurrentUserId()) {
            message.setStatus(1); // READ
        }
        return "redirect:/messages/index?filter=inbox";
    }

    // Архивировать сообщение
    @PostMapping("/archive")
    public String archiveMessage(@RequestParam int id) {
        Message message = findMessageById(id);
        if (message != null && message.getToUser() != null &&
                message.getToUser().getUserId() == getCurrentUserId() &&
                message.getStatus() != 2) { // не ARCHIVED
            message.setStatus(2); // ARCHIVED
        }
        return "redirect:/messages/index?filter=inbox";
    }

    // Восстановить из архива
    @PostMapping("/restore")
    public String restoreMessage(@RequestParam int id) {
        Message message = findMessageById(id);
        if (message != null && message.getToUser() != null &&
                message.getToUser().getUserId() == getCurrentUserId() &&
                message.getStatus() == 2) { // ARCHIVED
            message.setStatus(1); // READ
        }
        return "redirect:/messages/index?filter=archive";
    }

    // Удалить сообщение (только отправленные со статусом NEW)
    @PostMapping("/delete")
    public String deleteMessage(@RequestParam int id) {
        Message message = findMessageById(id);
        if (message != null && message.getFromUser() != null &&
                message.getFromUser().getUserId() == getCurrentUserId() &&
                message.getStatus() == 0) { // NEW
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
        u.setPassword("1234");

        Role role = new Role();
        role.setRoleName(roleName);
        u.setRole(role);

        return u;
    }
}