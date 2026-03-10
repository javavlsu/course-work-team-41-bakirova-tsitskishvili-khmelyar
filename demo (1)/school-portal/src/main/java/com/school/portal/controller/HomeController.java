package com.school.portal.controller;

import com.school.portal.model.Announcement;
import com.school.portal.model.SchoolClass;
import com.school.portal.model.User;
import com.school.portal.model.StudentClass;
import com.school.portal.repository.AnnouncementRepository;
import com.school.portal.repository.UserRepository;
import com.school.portal.repository.StudentClassRepository;
import com.school.portal.repository.SchoolClassRepository;
import com.school.portal.repository.StudentParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchoolClassRepository schoolClassRepository;

    @Autowired
    private StudentParentRepository studentParentRepository;

    @GetMapping("/")
    public String index(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        String username = auth.getName();
        User currentUser = userRepository.findByLogin(username).orElse(null);

        boolean isTeacher = false;

        if (currentUser != null) {
            if (currentUser.getSupervisedClasses() != null &&
                    !currentUser.getSupervisedClasses().isEmpty()) {

                isTeacher = true;
            }
        }

        model.addAttribute("isTeacher", isTeacher);

        model.addAttribute("title", "Главная");
        model.addAttribute("activePage", "home");

        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);

        List<Announcement> announcements = List.of();

        if (currentUser != null) {

            String role = currentUser.getRole().getRoleName();

            if (role.equals("STUDENT")) {

                // класс ученика
                SchoolClass schoolClass =
                        schoolClassRepository.findClassByStudentId(currentUser.getUserId())
                                .orElse(null);

                if (schoolClass != null) {
                    announcements =
                            announcementRepository.findRecentAnnouncementsForClass(
                                    schoolClass,
                                    twoWeeksAgo
                            );
                }

            } else if (role.equals("TEACHER")) {

                // класс где он классный руководитель
                List<SchoolClass> classes =
                        schoolClassRepository.findByClassTeacher(currentUser);

                if (!classes.isEmpty()) {

                    SchoolClass schoolClass = classes.get(0);

                    announcements =
                            announcementRepository.findRecentAnnouncementsForClass(
                                    schoolClass,
                                    twoWeeksAgo
                            );

                    model.addAttribute("isClassTeacher", true);
                }
            } else if (role.equals("PARENT")) {

                List<User> children =
                        studentParentRepository.findStudentsByParentId(currentUser.getUserId());

                List<Announcement> parentAnnouncements = new ArrayList<>();

                for (User child : children) {

                    SchoolClass schoolClass =
                            schoolClassRepository.findClassByStudentId(child.getUserId())
                                    .orElse(null);

                    if (schoolClass != null) {

                        parentAnnouncements.addAll(
                                announcementRepository.findRecentAnnouncementsForClass(
                                        schoolClass,
                                        twoWeeksAgo
                                )
                        );
                    }
                }

                announcements = parentAnnouncements;
            }
        }

        model.addAttribute("announcements", announcements);
        model.addAttribute("content", "home");

        return "layout";
    }

    @PostMapping("/create-announcement")
    public String createAnnouncement(
            @RequestParam String title,
            @RequestParam String text) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = auth.getName();
        User currentUser = userRepository.findByLogin(username).orElse(null);

        if (currentUser == null) {
            return "redirect:/login";
        }

        // получаем класс где он классный руководитель
        List<SchoolClass> classes = schoolClassRepository.findByClassTeacher(currentUser);

        if (classes.isEmpty()) {
            return "redirect:/?error=not_class_teacher";
        }

        SchoolClass schoolClass = classes.get(0);

        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setText(text);
        announcement.setSchoolClass(schoolClass);

        announcementRepository.save(announcement);

        return "redirect:/";
    }
}