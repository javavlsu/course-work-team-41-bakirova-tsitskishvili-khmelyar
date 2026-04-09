package com.school.portal.controller;

import com.school.portal.model.*;
import com.school.portal.model.dto.ProfileViewModel;
import com.school.portal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.school.portal.model.enums.MessageStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchoolClassRepository classRepository;

    @Autowired
    private StudentClassRepository studentClassRepository;

    @Autowired
    private StudentParentRepository studentParentRepository;

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/index")
    public String index(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentLogin = auth.getName();

        User currentUser = userRepository.findByLogin(currentLogin)
                .orElse(null);

        if (currentUser == null) {
            model.addAttribute("errorMessage", "Пользователь не найден");
            return "error";
        }

        ProfileViewModel profileModel = new ProfileViewModel();
        profileModel.setUserId(currentUser.getUserId());
        profileModel.setLastName(currentUser.getLastName());
        profileModel.setFirstName(currentUser.getFirstName());
        profileModel.setMiddleName(currentUser.getMiddleName());
        profileModel.setLogin(currentUser.getLogin());
        profileModel.setEmail(currentUser.getEmail());
        profileModel.setPhone(currentUser.getPhone());
        profileModel.setBirthDate(currentUser.getBirthDate());
        profileModel.setRoleName(currentUser.getRole() != null ? currentUser.getRole().getRoleName() : "Неизвестно");
        profileModel.setInfo(currentUser.getInfo());
        profileModel.setCoins(currentUser.getCoins());

        String homeroomTeacherName = null;
        String roleName = currentUser.getRole() != null ? currentUser.getRole().getRoleName() : "";

        if ("STUDENT".equals(roleName)) {
            Optional<StudentClass> studentClass = studentClassRepository.findByStudentUserId(currentUser.getUserId());
            if (studentClass.isPresent()) {
                SchoolClass schoolClass = studentClass.get().getSchoolClass();
                profileModel.setClassInfo(schoolClass.getClassName());

                if (schoolClass.getClassTeacher() != null) {
                    homeroomTeacherName = schoolClass.getClassTeacher().getFullName();
                }
            }
        } else if ("TEACHER".equals(roleName)) {
            var classes = classRepository.findByClassTeacher(currentUser);
            if (!classes.isEmpty()) {
                profileModel.setClassInfo("Классный руководитель: " +
                        classes.stream().map(SchoolClass::getClassName).reduce((a, b) -> a + ", " + b).orElse(""));
            }
        } else if ("PARENT".equals(roleName)) {
            var children = studentParentRepository.findByParentUserId(currentUser.getUserId());
            if (!children.isEmpty()) {
                var student = children.get(0).getStudent();
                profileModel.setStudentInfo(student.getFullName());

                Optional<StudentClass> studentClass = studentClassRepository.findByStudentUserId(student.getUserId());
                if (studentClass.isPresent() && studentClass.get().getSchoolClass().getClassTeacher() != null) {
                    homeroomTeacherName = studentClass.get().getSchoolClass().getClassTeacher().getFullName();
                }
            }
        }

        User director = userRepository.findByRole_RoleName("DIRECTOR").stream().findFirst().orElse(null);
        boolean isDirector = "DIRECTOR".equals(roleName);

        long unreadCount = messageRepository.countUnreadMessages(currentUser.getUserId());

        model.addAttribute("viewModel", profileModel);
        model.addAttribute("homeroomTeacher", homeroomTeacherName);
        model.addAttribute("directorId", director != null ? director.getUserId() : null);
        model.addAttribute("isDirector", isDirector);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("title", "Мой профиль");
        model.addAttribute("activePage", "profile");
        model.addAttribute("content", "profile/index");

        return "layout";
    }

    @PostMapping("/send-message-to-director")
    @ResponseBody
    public Map<String, Object> sendMessageToDirector(@RequestParam String body) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentLogin = auth.getName();

            User currentUser = userRepository.findByLogin(currentLogin)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            User director = userRepository.findByRole_RoleName("DIRECTOR").stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Директор не найден"));

            if (body == null || body.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Сообщение не может быть пустым.");
                return response;
            }

            Message message = new Message();
            message.setFromUser(currentUser);
            message.setToUser(director);
            message.setMessageText(body.trim());
            message.setSentAt(java.time.LocalDateTime.now());
            message.setStatus(MessageStatus.NEW); // NEW

            messageRepository.save(message);

            response.put("success", true);
            response.put("message", "Сообщение директору успешно отправлено.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Произошла ошибка: " + e.getMessage());
        }

        return response;
    }
}