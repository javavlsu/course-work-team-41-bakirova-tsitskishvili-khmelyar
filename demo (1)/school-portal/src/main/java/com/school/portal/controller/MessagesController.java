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
            case "archive":
                messages = messageRepository.findArchivedMessages(currentUser.getUserId());
                break;
            default:
                messages = messageRepository.findInboxMessages(currentUser.getUserId());
        }

        // Получаем все роли для формы
        List<String> roles = roleRepository.findAll().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        model.addAttribute("messages", messages);
        model.addAttribute("filter", filter);
        model.addAttribute("recipientRoles", roles);
        model.addAttribute("title", "Сообщения");
        model.addAttribute("activePage", "messages");
        model.addAttribute("content", "messages/index");

        return "layout";
    }

    @GetMapping("/search-user")
    @ResponseBody
    public List<Map<String, Object>> searchUser(
            @RequestParam String fullName,
            @RequestParam String role) {

        List<Map<String, Object>> result = new ArrayList<>();

        List<User> users = userRepository.findByRole_RoleName(role);

        String searchLower = fullName.toLowerCase().trim();

        List<User> filteredUsers = users.stream()
                .filter(u -> u.getFullName().toLowerCase().contains(searchLower))
                .limit(10)
                .collect(Collectors.toList());

        for (User user : filteredUsers) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", user.getUserId());
            userMap.put("fullName", user.getFullName());
            result.add(userMap);
        }

        return result;
    }

    @PostMapping("/create")
    public String createMessage(@ModelAttribute SendMessageViewModel model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User fromUser = userRepository.findByLogin(username)
                    .orElseThrow(() -> new RuntimeException("Отправитель не найден"));

            User toUser = userRepository.findById(model.getRecipientId())
                    .orElseThrow(() -> new RuntimeException("Получатель не найден"));

            Message message = new Message();
            message.setFromUser(fromUser);
            message.setToUser(toUser);
            message.setMessageText(model.getBody());
            message.setSentAt(LocalDateTime.now());
            message.setStatus(0); // NEW

            messageRepository.save(message);

            return "redirect:/messages/index?filter=sent&success=true";
        } catch (Exception e) {
            return "redirect:/messages/index?filter=sent&error=true";
        }
    }

    @PostMapping("/mark-as-read")
    public String markAsRead(@RequestParam int id) {
        try {
            messageRepository.findById(id).ifPresent(message -> {
                message.setStatus(1); // READ
                messageRepository.save(message);
            });
        } catch (Exception e) {
            // ignore
        }
        return "redirect:/messages/index?filter=inbox";
    }

    @PostMapping("/archive")
    public String archiveMessage(@RequestParam int id) {
        try {
            messageRepository.findById(id).ifPresent(message -> {
                message.setStatus(2); // ARCHIVED
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
                message.setStatus(1); // READ
                messageRepository.save(message);
            });
        } catch (Exception e) {
            // ignore
        }
        return "redirect:/messages/index?filter=archive";
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