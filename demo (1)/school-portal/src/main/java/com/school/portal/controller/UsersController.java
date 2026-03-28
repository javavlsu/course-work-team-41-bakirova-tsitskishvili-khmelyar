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
    private StudentParentRepository studentParentRepository;

    @Autowired
    private StudentClassRepository studentClassRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Метод для получения русского названия роли
    private String getRussianRoleName(String roleName) {
        if (roleName == null) return "Не указана";
        switch (roleName) {
            case "DIRECTOR": return "Директор";
            case "ADMIN": return "Администратор";
            case "TEACHER": return "Учитель";
            case "STUDENT": return "Ученик";
            case "PARENT": return "Родитель";
            default: return roleName;
        }
    }

    // ========== ОСНОВНАЯ СТРАНИЦА ==========
    @GetMapping("/index")
    public String index(@RequestParam(value = "roleFilter", required = false) String roleFilter,
                        @RequestParam(value = "classFilter", required = false) Integer classFilter,
                        @RequestParam(value = "searchTerm", required = false) String searchTerm,
                        Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = userRepository.findByLogin(currentUsername).orElse(null);
        boolean isAdmin = currentUser != null && "ADMIN".equals(currentUser.getRole().getRoleName());

        List<User> users = userRepository.findAll();

        // Фильтрация по роли
        if (roleFilter != null && !roleFilter.isEmpty()) {
            users = users.stream()
                    .filter(u -> u.getRole() != null && roleFilter.equals(u.getRole().getRoleName()))
                    .collect(Collectors.toList());
        }

        // Фильтрация по классу (только для учеников)
        if (classFilter != null) {
            Set<Integer> studentIdsInClass = studentClassRepository.findStudentsByClassId(classFilter)
                    .stream()
                    .map(User::getUserId)
                    .collect(Collectors.toSet());
            users = users.stream()
                    .filter(u -> studentIdsInClass.contains(u.getUserId()))
                    .collect(Collectors.toList());
        }

        // Поиск по ФИО
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String searchLower = searchTerm.toLowerCase().trim();
            users = users.stream()
                    .filter(u -> u.getFullName().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        users.sort(Comparator.comparing(User::getLastName));

        // Подготавливаем данные для отображения
        List<Map<String, Object>> usersWithDetails = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", user.getUserId());
            userMap.put("fullName", user.getFullName());
            userMap.put("roleName", user.getRole() != null ? user.getRole().getRoleName() : "");
            userMap.put("roleNameRu", getRussianRoleName(user.getRole() != null ? user.getRole().getRoleName() : ""));
            userMap.put("email", user.getEmail());
            userMap.put("phone", user.getPhone());
            userMap.put("birthDate", user.getBirthDate() != null ? user.getBirthDate().toString() : "");
            userMap.put("login", user.getLogin());
            userMap.put("password", isAdmin ? user.getPassword() : null); // Только ADMIN видит пароль
            userMap.put("info", user.getInfo());
            userMap.put("coins", user.getCoins());
            usersWithDetails.add(userMap);
        }

        // Список ролей для фильтра с русскими названиями
        List<Map<String, String>> availableRolesWithRu = new ArrayList<>();
        List<Role> roles = roleRepository.findAll();
        for (Role role : roles) {
            Map<String, String> roleMap = new HashMap<>();
            roleMap.put("name", role.getRoleName());
            roleMap.put("nameRu", getRussianRoleName(role.getRoleName()));
            availableRolesWithRu.add(roleMap);
        }

        model.addAttribute("users", usersWithDetails);
        model.addAttribute("availableRoles", availableRolesWithRu);
        model.addAttribute("schoolClasses", classRepository.findAll());
        model.addAttribute("roleFilter", roleFilter);
        model.addAttribute("classFilter", classFilter);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("title", "Администрирование пользователей");
        model.addAttribute("activePage", "users");
        model.addAttribute("content", "users/index");

        return "layout";
    }

    // ========== AJAX: ПОЛУЧЕНИЕ ДЕТАЛЕЙ ПОЛЬЗОВАТЕЛЯ ==========
    @GetMapping("/get-user-details")
    @ResponseBody
    public Map<String, Object> getUserDetails(@RequestParam("id") Integer userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();
            User currentUser = userRepository.findByLogin(currentUsername).orElse(null);
            boolean isAdmin = currentUser != null && "ADMIN".equals(currentUser.getRole().getRoleName());

            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserId());
            userData.put("fullName", user.getFullName());
            userData.put("role", getRussianRoleName(user.getRole() != null ? user.getRole().getRoleName() : ""));
            userData.put("email", user.getEmail());
            userData.put("phone", user.getPhone());
            userData.put("birthDate", user.getBirthDate() != null ? user.getBirthDate().toString() : "");
            userData.put("username", user.getLogin());
            userData.put("password", isAdmin ? user.getPassword() : null);
            userData.put("info", user.getInfo());
            userData.put("coins", user.getCoins());

            response.put("success", true);
            response.put("user", userData);
            response.put("isAdmin", isAdmin);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ========== AJAX: ПОЛУЧЕНИЕ ФОРМЫ РЕДАКТИРОВАНИЯ ==========
    @GetMapping("/edit-user-partial")
    @ResponseBody
    public Map<String, Object> editUserPartial(@RequestParam("id") Integer userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Map<String, Object> viewModel = new HashMap<>();
            viewModel.put("userId", user.getUserId());
            viewModel.put("lastName", user.getLastName());
            viewModel.put("firstName", user.getFirstName());
            viewModel.put("middleName", user.getMiddleName());
            viewModel.put("email", user.getEmail());
            viewModel.put("phone", user.getPhone());
            viewModel.put("birthDate", user.getBirthDate() != null ? user.getBirthDate().toString() : "");
            viewModel.put("info", user.getInfo());

            response.put("success", true);
            response.put("viewModel", viewModel);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // ========== AJAX: РЕДАКТИРОВАНИЕ ПОЛЬЗОВАТЕЛЯ ==========
    @PostMapping("/edit-user")
    @ResponseBody
    public Map<String, Object> editUser(@RequestParam("userId") Integer userId,
                                        @RequestParam(value = "lastName", required = false) String lastName,
                                        @RequestParam(value = "firstName", required = false) String firstName,
                                        @RequestParam(value = "middleName", required = false) String middleName,
                                        @RequestParam(value = "email", required = false) String email,
                                        @RequestParam(value = "phone", required = false) String phone,
                                        @RequestParam(value = "birthDate", required = false) String birthDate,
                                        @RequestParam(value = "info", required = false) String info,
                                        @RequestParam(value = "password", required = false) String password) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            user.setLastName(lastName);
            user.setFirstName(firstName);
            user.setMiddleName(middleName);
            user.setEmail(email);
            user.setPhone(phone);
            if (birthDate != null && !birthDate.isEmpty()) {
                user.setBirthDate(LocalDate.parse(birthDate));
            }
            user.setInfo(info);

            // Если передан новый пароль и пользователь - ADMIN, меняем пароль
            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            userRepository.save(user);

            response.put("success", true);
            response.put("message", "Данные пользователя успешно обновлены");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }
        return response;
    }

    // ========== AJAX: УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ==========
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteUser(@RequestParam("id") Integer userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Запрещаем удалять директора и админа
            if (user.getRole() != null &&
                    ("DIRECTOR".equals(user.getRole().getRoleName()) ||
                            "ADMIN".equals(user.getRole().getRoleName()))) {
                response.put("success", false);
                response.put("message", "Невозможно удалить администратора или директора");
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

    @GetMapping("/create")
    public String create(Model model) {
        Map<String, Object> viewModel = new HashMap<>();

        // Список ролей
        List<Map<String, Object>> availableRoles = new ArrayList<>();
        List<Role> roles = roleRepository.findAll();
        for (Role role : roles) {
            Map<String, Object> roleMap = new HashMap<>();
            roleMap.put("key", role.getRoleId());
            roleMap.put("value", role.getRoleName());
            availableRoles.add(roleMap);
        }
        viewModel.put("availableRoles", availableRoles);

        // Список классов
        List<Map<String, Object>> schoolClasses = new ArrayList<>();
        List<SchoolClass> classes = classRepository.findAll();
        for (SchoolClass schoolClass : classes) {
            Map<String, Object> classMap = new HashMap<>();
            classMap.put("key", schoolClass.getClassId());
            classMap.put("value", schoolClass.getClassName());
            schoolClasses.add(classMap);
        }
        viewModel.put("schoolClasses", schoolClasses);

        // Список всех учеников
        List<Map<String, Object>> allStudents = new ArrayList<>();
        List<User> students = userRepository.findByRole_RoleName("STUDENT");
        for (User student : students) {
            Map<String, Object> studentMap = new HashMap<>();
            studentMap.put("key", student.getUserId());
            studentMap.put("value", student.getFullName());
            allStudents.add(studentMap);
        }
        viewModel.put("allStudents", allStudents);

        model.addAttribute("viewModel", viewModel);
        model.addAttribute("title", "Создание пользователя");
        model.addAttribute("activePage", "users");
        model.addAttribute("content", "users/create");

        return "layout";  // Возвращаем layout, который подключает create.html
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute UserCreateRequest request,
                             RedirectAttributes redirectAttributes) {
        try {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Роль не найдена"));

            String login = request.getLogin();
            if (login == null || login.isEmpty()) {
                login = generateLogin(request.getFirstName(), request.getLastName());
            }

            String password = request.getPassword();
            if (password == null || password.isEmpty()) {
                password = generateRandomPassword(8);
            }

            User user = new User();
            user.setLogin(login);
            user.setPassword(passwordEncoder.encode(password));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setMiddleName(request.getMiddleName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setBirthDate(request.getBirthDate());
            user.setInfo(request.getInfo());
            user.setRole(role);
            user.setCoins(0);

            user = userRepository.save(user);

            if ("STUDENT".equals(role.getRoleName()) && request.getClassId() != null) {
                StudentClass studentClass = new StudentClass();
                studentClass.setStudent(user);
                SchoolClass schoolClass = classRepository.findById(request.getClassId()).orElse(null);
                studentClass.setSchoolClass(schoolClass);
                studentClassRepository.save(studentClass);
            } else if ("PARENT".equals(role.getRoleName()) && request.getStudentIdForParent() != null) {
                User student = userRepository.findById(request.getStudentIdForParent()).orElse(null);
                if (student != null) {
                    StudentParent studentParent = new StudentParent();
                    studentParent.setStudent(student);
                    studentParent.setParent(user);
                    studentParentRepository.save(studentParent);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Пользователь успешно создан. Логин: " + login + ", пароль: " + password);
            return "redirect:/users/index";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка создания пользователя: " + e.getMessage());
            return "redirect:/users/create";
        }
    }

    @PostMapping("/add-class")
    @ResponseBody
    public Map<String, Object> addClass(@ModelAttribute ClassRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            SchoolClass schoolClass = new SchoolClass();
            schoolClass.setClassNumber(request.getClassNumber());
            schoolClass.setClassLetter(request.getClassLetter().toUpperCase());
            if (request.getClassTeacherId() != null) {
                User teacher = userRepository.findById(request.getClassTeacherId()).orElse(null);
                schoolClass.setClassTeacher(teacher);
            }
            classRepository.save(schoolClass);

            response.put("success", true);
            response.put("message", "Класс успешно добавлен");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/add-subject")
    @ResponseBody
    public Map<String, Object> addSubject(@ModelAttribute SubjectRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Subject subject = new Subject();
            subject.setSubjectName(request.getSubjectName());
            subjectRepository.save(subject);

            response.put("success", true);
            response.put("message", "Предмет успешно добавлен");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/manage-classes-partial")
    @ResponseBody
    public Map<String, Object> manageClassesPartial() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> schoolClasses = new ArrayList<>();
            List<SchoolClass> classes = classRepository.findAll();
            for (SchoolClass c : classes) {
                Map<String, Object> map = new HashMap<>();
                map.put("classId", c.getClassId());
                map.put("className", c.getClassName());
                map.put("classNumber", c.getClassNumber());
                map.put("classLetter", c.getClassLetter());
                map.put("classTeacherId", c.getClassTeacher() != null ? c.getClassTeacher().getUserId() : null);
                map.put("classTeacherName", c.getClassTeacher() != null ? c.getClassTeacher().getFullName() : "Не назначен");
                // Проверяем, есть ли ученики в классе
                List<User> students = studentClassRepository.findStudentsByClassId(c.getClassId());
                map.put("studentCount", students.size());
                map.put("canDelete", students.isEmpty());
                schoolClasses.add(map);
            }

            List<Map<String, Object>> availableTeachers = new ArrayList<>();
            List<User> teachers = userRepository.findByRole_RoleName("TEACHER");
            for (User t : teachers) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", t.getUserId());
                map.put("text", t.getFullName());
                availableTeachers.add(map);
            }

            // Список всех классов для выпадающего списка при перемещении учеников
            List<Map<String, Object>> allClasses = new ArrayList<>();
            for (SchoolClass c : classes) {
                Map<String, Object> map = new HashMap<>();
                map.put("value", c.getClassId());
                map.put("text", c.getClassName());
                allClasses.add(map);
            }

            response.put("success", true);
            response.put("schoolClasses", schoolClasses);
            response.put("availableTeachers", availableTeachers);
            response.put("allClasses", allClasses);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/manage-subjects-partial")
    @ResponseBody
    public Map<String, Object> manageSubjectsPartial() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> subjects = new ArrayList<>();
            List<Subject> subjectList = subjectRepository.findAll();
            for (Subject s : subjectList) {
                Map<String, Object> map = new HashMap<>();
                map.put("subjectId", s.getSubjectId());
                map.put("subjectName", s.getSubjectName());
                subjects.add(map);
            }

            response.put("success", true);
            response.put("subjects", subjects);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/delete-class")
    @ResponseBody
    public Map<String, Object> deleteClass(@RequestParam("classId") Integer classId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Проверяем, есть ли ученики в классе
            List<User> studentsInClass = studentClassRepository.findStudentsByClassId(classId);

            if (!studentsInClass.isEmpty()) {
                response.put("success", false);
                response.put("message", "Невозможно удалить класс, так как в нем есть ученики. Сначала переместите или удалите учеников.");
                return response;
            }

            classRepository.deleteById(classId);
            response.put("success", true);
            response.put("message", "Класс успешно удален");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/delete-subject")
    @ResponseBody
    public Map<String, Object> deleteSubject(@RequestParam("subjectId") Integer subjectId) {
        Map<String, Object> response = new HashMap<>();
        try {
            subjectRepository.deleteById(subjectId);
            response.put("success", true);
            response.put("message", "Предмет успешно удален");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/edit-class")
    @ResponseBody
    public Map<String, Object> editClass(@ModelAttribute ClassRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            SchoolClass schoolClass = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new RuntimeException("Класс не найден"));

            // Обновляем данные класса
            schoolClass.setClassNumber(request.getClassNumber());
            schoolClass.setClassLetter(request.getClassLetter().toUpperCase());

            // Обновляем классного руководителя
            if (request.getClassTeacherId() != null) {
                User teacher = userRepository.findById(request.getClassTeacherId()).orElse(null);
                schoolClass.setClassTeacher(teacher);
            } else {
                schoolClass.setClassTeacher(null);
            }

            classRepository.save(schoolClass);

            response.put("success", true);
            response.put("message", "Класс успешно обновлен");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/move-students")
    @ResponseBody
    public Map<String, Object> moveStudents(@RequestParam("oldClassId") Integer oldClassId,
                                            @RequestParam("newClassId") Integer newClassId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Проверяем существование классов
            SchoolClass oldClass = classRepository.findById(oldClassId)
                    .orElseThrow(() -> new RuntimeException("Исходный класс не найден"));
            SchoolClass newClass = classRepository.findById(newClassId)
                    .orElseThrow(() -> new RuntimeException("Целевой класс не найден"));

            // Получаем всех учеников из старого класса
            List<User> students = studentClassRepository.findStudentsByClassId(oldClassId);

            // Перемещаем каждого ученика в новый класс
            for (User student : students) {
                // Удаляем старую запись
                StudentClass oldStudentClass = studentClassRepository.findByStudentUserId(student.getUserId())
                        .orElse(null);
                if (oldStudentClass != null) {
                    studentClassRepository.delete(oldStudentClass);
                }

                // Создаем новую запись
                StudentClass newStudentClass = new StudentClass();
                newStudentClass.setStudent(student);
                newStudentClass.setSchoolClass(newClass);
                studentClassRepository.save(newStudentClass);
            }

            response.put("success", true);
            response.put("message", students.size() + " учеников успешно перемещены в класс " + newClass.getClassName());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }
        return response;
    }


    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========
    private String generateLogin(String firstName, String lastName) {
        String baseLogin = (firstName != null && !firstName.isEmpty() ? firstName.toLowerCase().charAt(0) : "") +
                (lastName != null ? lastName.toLowerCase() : "");

        String login = baseLogin;
        int counter = 1;

        while (userRepository.findByLogin(login).isPresent()) {
            login = baseLogin + counter;
            counter++;
        }

        return login;
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    // ========== ВСПОМОГАТЕЛЬНЫЙ КЛАСС ДЛЯ СОЗДАНИЯ ПОЛЬЗОВАТЕЛЯ ==========
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
        private Integer classId;
        private Integer studentIdForParent;

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

        public Integer getClassId() { return classId; }
        public void setClassId(Integer classId) { this.classId = classId; }

        public Integer getStudentIdForParent() { return studentIdForParent; }
        public void setStudentIdForParent(Integer studentIdForParent) { this.studentIdForParent = studentIdForParent; }
    }

    // ========== ВСПОМОГАТЕЛЬНЫЙ КЛАСС ДЛЯ КЛАССА ==========
    public static class ClassRequest {
        private Integer classId;
        private Integer classNumber;
        private String classLetter;
        private Integer classTeacherId;
        private Integer oldClassId; // для переноса учеников

        public Integer getClassId() { return classId; }
        public void setClassId(Integer classId) { this.classId = classId; }

        public Integer getClassNumber() { return classNumber; }
        public void setClassNumber(Integer classNumber) { this.classNumber = classNumber; }

        public String getClassLetter() { return classLetter; }
        public void setClassLetter(String classLetter) { this.classLetter = classLetter; }

        public Integer getClassTeacherId() { return classTeacherId; }
        public void setClassTeacherId(Integer classTeacherId) { this.classTeacherId = classTeacherId; }

        public Integer getOldClassId() { return oldClassId; }
        public void setOldClassId(Integer oldClassId) { this.oldClassId = oldClassId; }
    }

    // ========== ВСПОМОГАТЕЛЬНЫЙ КЛАСС ДЛЯ ПРЕДМЕТА ==========
    public static class SubjectRequest {
        private Integer subjectId;
        private String subjectName;

        public Integer getSubjectId() { return subjectId; }
        public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    }
}