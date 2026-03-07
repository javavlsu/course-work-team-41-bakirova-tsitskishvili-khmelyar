package com.school.portal.controller;

import com.school.portal.model.*;
import com.school.portal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SchoolClassRepository classRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/index")
    public String index(@RequestParam(value = "roleFilter", required = false) String roleFilter,
                        @RequestParam(value = "searchTerm", required = false) String searchTerm,
                        Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        User currentUser = userRepository.findByLogin(currentUsername).orElse(null);

        List<User> users = userRepository.findAll();

        // Фильтрация
        if (roleFilter != null && !roleFilter.isEmpty() && !"Все роли".equals(roleFilter)) {
            users = users.stream()
                    .filter(u -> u.getRole() != null && roleFilter.equals(u.getRole().getRoleName()))
                    .collect(Collectors.toList());
        }

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchLower = searchTerm.toLowerCase().trim();
            users = users.stream()
                    .filter(u -> u.getFullName().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        users.sort(Comparator.comparing(User::getLastName));

        model.addAttribute("users", users);
        model.addAttribute("availableRoles", roleRepository.findAll());
        model.addAttribute("roleFilter", roleFilter);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("title", "Администрирование пользователей");
        model.addAttribute("activePage", "users");
        model.addAttribute("content", "users/index");

        return "layout";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("availableRoles", roleRepository.findAll());
        model.addAttribute("title", "Создание пользователя");
        model.addAttribute("activePage", "users");
        model.addAttribute("content", "users/create");
        return "layout";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute UserCreateRequest request,
                             RedirectAttributes redirectAttributes) {
        try {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Роль не найдена"));

            User user = new User();
            user.setLogin(request.getLogin());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setMiddleName(request.getMiddleName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setBirthDate(request.getBirthDate());
            user.setInfo(request.getInfo());
            user.setRole(role);
            user.setCoins(0);

            userRepository.save(user);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Пользователь успешно создан");
            return "redirect:/users/index";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка создания пользователя: " + e.getMessage());
            return "redirect:/users/create";
        }
    }

    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteUser(@RequestParam("id") int userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            if (user.getRole() != null && "DIRECTOR".equals(user.getRole().getRoleName())) {
                response.put("success", false);
                response.put("message", "Невозможно удалить директора");
                return response;
            }

            userRepository.deleteById(userId);
            response.put("success", true);
            response.put("message", "Пользователь успешно удален");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }

        return response;
    }

    // Вспомогательный класс для создания пользователя
    public static class UserCreateRequest {
        private String login;
        private String password;
        private String firstName;
        private String lastName;
        private String middleName;
        private String email;
        private String phone;
        private LocalDate birthDate;
        private String info;
        private int roleId;

        // Геттеры и сеттеры
        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getMiddleName() { return middleName; }
        public void setMiddleName(String middleName) { this.middleName = middleName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
        public String getInfo() { return info; }
        public void setInfo(String info) { this.info = info; }
        public int getRoleId() { return roleId; }
        public void setRoleId(int roleId) { this.roleId = roleId; }
    }
}