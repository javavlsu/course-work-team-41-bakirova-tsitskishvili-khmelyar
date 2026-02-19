package com.school.portal.controller;

import com.school.portal.model.Announcement;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private List<Announcement> announcements = new ArrayList<>();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public HomeController() {
        // Инициализация тестовыми данными
        announcements.add(new Announcement(1L,
                "Родительское собрание",
                "Уважаемые родители! Приглашаем вас на родительское собрание, которое состоится 15 мая в 18:00 в актовом зале школы.",
                LocalDateTime.now().minusDays(1)));

        announcements.add(new Announcement(2L,
                "Внимание! Изменения в расписании",
                "В связи с проведением олимпиады по математике изменено расписание уроков на среду.",
                LocalDateTime.now().minusHours(3)));
    }

    @GetMapping("/")
    public String index(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Проверка аутентификации
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        // Устанавливаем заголовок и активную страницу
        model.addAttribute("title", "Главная");
        model.addAttribute("activePage", "home");

        // Проверяем роль учителя
        boolean isTeacher = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        model.addAttribute("isTeacher", isTeacher);

        // Фильтруем и форматируем объявления
        List<Announcement> recentAnnouncements = announcements.stream()
                .filter(a -> a.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7)))
                .collect(Collectors.toList());

        recentAnnouncements.forEach(a -> {
            a.setFormattedDate(a.getCreatedAt().format(DATE_FORMATTER));
        });

        model.addAttribute("announcements", recentAnnouncements);
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

        // Проверяем роль учителя
        boolean isTeacher = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));

        if (!isTeacher) {
            return "redirect:/?error=not_authorized";
        }

        // Добавляем новое объявление
        Long newId = announcements.stream()
                .mapToLong(Announcement::getId)
                .max()
                .orElse(0L) + 1;

        Announcement newAnnouncement = new Announcement(
                newId, title, text, LocalDateTime.now());

        announcements.add(newAnnouncement);

        return "redirect:/";
    }
}