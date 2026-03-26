package com.school.portal.controller;

import com.school.portal.model.*;
import com.school.portal.model.dto.SendMessageViewModel;
import com.school.portal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.school.portal.model.enums.MessageStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/messages")
public class MessagesController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/index")
    public String index(@RequestParam(value = "filter", defaultValue = "inbox") String filter,
                        Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Message> messages;

        switch (filter) {
            case "inbox":
                messages = messageRepository.findInboxMessages(currentUser.getUserId());
                break;
            case "sent":
                messages = messageRepository.findSentMessages(currentUser.getUserId());
                break;
            case "elect":
                messages = messageRepository.findElectMessages(currentUser.getUserId());
                break;
            default:
                messages = messageRepository.findInboxMessages(currentUser.getUserId());
        }

        // Получаем все роли для формы
        List<String> role = roleRepository.findAll().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        model.addAttribute("messages", messages);
        model.addAttribute("filter", filter);
        model.addAttribute("recipientRoles", role);
        model.addAttribute("title", "Сообщения");
        model.addAttribute("activePage", "messages");
        model.addAttribute("content", "messages/index");

        return "layout";
    }

    @GetMapping("/search-user")
    @ResponseBody
    public List<Map<String, Object>> searchUser(
            @RequestParam String fullName,
            @RequestParam(required = false) String role) {

        List<User> users;
        // Если роль пришла как пустая строка или null - ищем по всем
        if (role == null || role.trim().isEmpty()) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findByRole_RoleName(role);
        }

        String searchLower = fullName.toLowerCase().trim();
        return users.stream()
                .filter(u -> u.getFullName().toLowerCase().contains(searchLower))
                .limit(10)
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("userId", user.getUserId());
                    userMap.put("fullName", user.getFullName());
                    return userMap;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/create")
    public String createMessage(@ModelAttribute SendMessageViewModel model,
                                @RequestParam(required = false) Integer parentMessageId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User fromUser = userRepository.findByLogin(username)
                    .orElseThrow(() -> new RuntimeException("Отправитель не найден"));

            User toUser = userRepository.findById(model.getRecipientId())
                    .orElseThrow(() -> new RuntimeException("Получатель не найден"));

            // 1. Создаем и сохраняем новое сообщение
            Message newMessage = new Message();
            newMessage.setFromUser(fromUser);
            newMessage.setToUser(toUser);
            newMessage.setMessageText(model.getBody());
            newMessage.setStatus(com.school.portal.model.enums.MessageStatus.NEW); // Используем Enum
            messageRepository.save(newMessage);

            // 2. Если это ответ, помечаем исходное сообщение как прочитанное
            if (parentMessageId != null) {
                messageRepository.findById(parentMessageId).ifPresent(oldMsg -> {
                    // Если оно еще "Новое", делаем "Прочитанным"
                    if (oldMsg.getStatus() == com.school.portal.model.enums.MessageStatus.NEW) {
                        oldMsg.setStatus(com.school.portal.model.enums.MessageStatus.READ);
                        messageRepository.save(oldMsg);
                    }
                });
            }

            return "redirect:/messages/index?filter=sent&success=true";
        } catch (Exception e) {
            return "redirect:/messages/index?filter=sent&error=true";
        }
    }

    @PostMapping("/mark-as-read")
    public String markAsRead(@RequestParam int id) {
        try {
            messageRepository.findById(id).ifPresent(message -> {
                message.setStatus(MessageStatus.READ); // READ
                messageRepository.save(message);
            });
        } catch (Exception e) {
            // ignore
        }
        return "redirect:/messages/index?filter=inbox";
    }

    @PostMapping("/elect")
    public String electMessage(@RequestParam int id) {
        try {
            messageRepository.findById(id).ifPresent(message -> {
                message.setStatus(MessageStatus.ELECT); // ELECT
                messageRepository.save(message);
            });
        } catch (Exception e) {
            // ignore
        }
        return "redirect:/messages/index?filter=inbox";
    }

    @PostMapping("/restore")
    public String restoreMessage(@RequestParam int id) {
        try {
            messageRepository.findById(id).ifPresent(message -> {
                message.setStatus(MessageStatus.READ); // READ
                messageRepository.save(message);
            });
        } catch (Exception e) {
            // ignore
        }
        return "redirect:/messages/index?filter=elect";
    }

    @PostMapping("/delete")
    public String deleteMessage(@RequestParam int id) {
        try {
            messageRepository.deleteById(id);
        } catch (Exception e) {
            // ignore
        }
        return "redirect:/messages/index?filter=sent";
    }
}