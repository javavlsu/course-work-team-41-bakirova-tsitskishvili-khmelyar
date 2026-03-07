package com.school.portal.controller;

import com.school.portal.model.Announcement;
import com.school.portal.model.User;
import com.school.portal.repository.AnnouncementRepository;
import com.school.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String index(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User currentUser = userRepository.findByLogin(username).orElse(null);

        model.addAttribute("title", "Главная");
        model.addAttribute("activePage", "home");

        boolean isTeacher = currentUser != null &&
                (currentUser.getRole().getRoleName().equals("TEACHER") ||
                        currentUser.getRole().getRoleName().equals("DIRECTOR"));
        model.addAttribute("isTeacher", isTeacher);

        // Получаем объявления
        List<Announcement> announcements;
        if (currentUser != null && currentUser.getRole().getRoleName().equals("STUDENT")) {
            // Для ученика - объявления его класса и общешкольные
            // Упрощенно - все объявления
            announcements = announcementRepository.findAll();
        } else {
            announcements = announcementRepository.findAll();
        }

        // Сортируем по дате (новые сверху)
        announcements.sort((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()));

        model.addAttribute("announcements", announcements);
        model.addAttribute("content", "home");

        return "layout";
    }

    @PostMapping("/create-announcement")
    public String createAnnouncement(
            @RequestParam String title,
            @RequestParam String text) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User currentUser = userRepository.findByLogin(username).orElse(null);

        // Проверяем роль учителя или директора
        boolean canCreate = currentUser != null &&
                (currentUser.getRole().getRoleName().equals("TEACHER") ||
                        currentUser.getRole().getRoleName().equals("DIRECTOR"));

        if (!canCreate) {
            return "redirect:/?error=not_authorized";
        }

        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setText(text);
        announcement.setCreatedAt(LocalDateTime.now());
        // announcement.setSchoolClass(null); // Общешкольное

        announcementRepository.save(announcement);

        return "redirect:/";
    }
}