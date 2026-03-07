package com.school.portal.controller;

import com.school.portal.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // Заглушечные данные
    private List<User> users = new ArrayList<>();
    private List<Role> roles = new ArrayList<>();
    private List<SchoolClass> schoolClasses = new ArrayList<>();
    private List<Subject> subjects = new ArrayList<>();

    public UsersController() {
        initializeDemoData();
    }

    private void initializeDemoData() {
        // Инициализация ролей
        Role teacherRole = new Role("Учитель");
        teacherRole.setRoleId(1);
        roles.add(teacherRole);

        Role directorRole = new Role("Директор");
        directorRole.setRoleId(2);
        roles.add(directorRole);

        Role studentRole = new Role("Ученик");
        studentRole.setRoleId(3);
        roles.add(studentRole);

        Role parentRole = new Role("Родитель");
        parentRole.setRoleId(4);
        roles.add(parentRole);

        // Инициализация классов
        SchoolClass class9A = new SchoolClass();
        class9A.setClassId(1);
        class9A.setClassNumber(9);
        class9A.setClassLetter("А");
        // Временно без классного руководителя
        schoolClasses.add(class9A);

        SchoolClass class9B = new SchoolClass();
        class9B.setClassId(2);
        class9B.setClassNumber(9);
        class9B.setClassLetter("Б");
        schoolClasses.add(class9B);

        SchoolClass class10A = new SchoolClass();
        class10A.setClassId(3);
        class10A.setClassNumber(10);
        class10A.setClassLetter("А");
        schoolClasses.add(class10A);

        SchoolClass class10B = new SchoolClass();
        class10B.setClassId(4);
        class10B.setClassNumber(10);
        class10B.setClassLetter("Б");
        schoolClasses.add(class10B);

        SchoolClass class11A = new SchoolClass();
        class11A.setClassId(5);
        class11A.setClassNumber(11);
        class11A.setClassLetter("А");
        schoolClasses.add(class11A);

        // Инициализация предметов
        Subject math = new Subject("Математика");
        math.setSubjectId(1);
        subjects.add(math);

        Subject russian = new Subject("Русский язык");
        russian.setSubjectId(2);
        subjects.add(russian);

        Subject physics = new Subject("Физика");
        physics.setSubjectId(3);
        subjects.add(physics);

        Subject chemistry = new Subject("Химия");
        chemistry.setSubjectId(4);
        subjects.add(chemistry);

        Subject history = new Subject("История");
        history.setSubjectId(5);
        subjects.add(history);

        Subject english = new Subject("Английский язык");
        english.setSubjectId(6);
        subjects.add(english);

        Subject informatics = new Subject("Информатика");
        informatics.setSubjectId(7);
        subjects.add(informatics);

        // Инициализация пользователей
        users.add(createMockUser(1, "teacher1", "Алексей", "Иванов", "Петрович", teacherRole,
                "teacher@school.ru", "+79991234567", LocalDate.of(1980, 5, 15),
                "Классный руководитель 9А, преподаватель математики"));

        users.add(createMockUser(2, "director", "Мария", "Петрова", "Сергеевна", directorRole,
                "director@school.ru", "+79997654321", LocalDate.of(1975, 3, 22),
                "Директор школы, кандидат педагогических наук"));

        users.add(createMockUser(3, "student1", "Дмитрий", "Сидоров", "Иванович", studentRole,
                "student1@school.ru", null, LocalDate.of(2007, 8, 10),
                "Успевающий ученик, участник олимпиад"));

        users.add(createMockUser(4, "student2", "Анна", "Кузнецова", "Владимировна", studentRole,
                "student2@school.ru", "+79998887766", LocalDate.of(2008, 2, 25),
                "Отличница, занимается музыкой"));

        users.add(createMockUser(5, "parent1", "Павел", "Смирнов", "Александрович", parentRole,
                "parent@mail.ru", "+79995554433", LocalDate.of(1982, 11, 5),
                "Председатель родительского комитета"));

        users.add(createMockUser(6, "teacher2", "Елена", "Федорова", "Дмитриевна", teacherRole,
                "teacher2@school.ru", "+79993332211", LocalDate.of(1985, 7, 30),
                "Преподаватель русского языка и литературы"));
    }

    // Главная страница списка пользователей
    @GetMapping("/index")
    public String index(@RequestParam(value = "roleFilter", required = false) String roleFilter,
                        @RequestParam(value = "classFilter", required = false) Integer classFilter,
                        @RequestParam(value = "searchTerm", required = false) String searchTerm,
                        Model model) {

        // Получаем ID текущего пользователя для отображения
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = users.stream()
                .filter(u -> u.getLogin().equals(currentUsername))
                .findFirst()
                .orElse(null);

        if (currentUser != null) {
            model.addAttribute("currentAuthUserId", String.valueOf(currentUser.getUserId()));
        } else {
            model.addAttribute("currentAuthUserId", "");
        }

        // Фильтрация пользователей
        List<User> filteredUsers = new ArrayList<>(users);

        if (roleFilter != null && !roleFilter.isEmpty() && !"Все роли".equals(roleFilter)) {
            filteredUsers = filteredUsers.stream()
                    .filter(u -> u.getRole() != null && roleFilter.equals(u.getRole().getRoleName()))
                    .collect(Collectors.toList());
        }

        if (classFilter != null) {
            // Здесь была бы логика фильтрации по классу
            // Для демо просто пропускаем всех
        }

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            final String searchTermFinal = searchTerm.toLowerCase().trim();
            filteredUsers = filteredUsers.stream()
                    .filter(u -> {
                        String fullName = (u.getFullName() != null ? u.getFullName().toLowerCase() : "");
                        String lastName = (u.getLastName() != null ? u.getLastName().toLowerCase() : "");
                        String firstName = (u.getFirstName() != null ? u.getFirstName().toLowerCase() : "");
                        String middleName = (u.getMiddleName() != null ? u.getMiddleName().toLowerCase() : "");

                        return fullName.contains(searchTermFinal) ||
                                lastName.contains(searchTermFinal) ||
                                firstName.contains(searchTermFinal) ||
                                middleName.contains(searchTermFinal);
                    })
                    .collect(Collectors.toList());
        }

        // Сортировка по фамилии
        filteredUsers.sort(Comparator.comparing(User::getLastName)
                .thenComparing(User::getFirstName));

        model.addAttribute("users", filteredUsers);
        model.addAttribute("availableRoles", roles);
        model.addAttribute("schoolClasses", schoolClasses);
        model.addAttribute("roleFilter", roleFilter);
        model.addAttribute("classFilter", classFilter);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("title", "Администрирование пользователей");
        model.addAttribute("activePage", "users");
        model.addAttribute("content", "users/index");

        return "layout";
    }

    // Получение деталей пользователя (AJAX)
    @GetMapping("/get-user-details")
    @ResponseBody
    public Map<String, Object> getUserDetails(@RequestParam("id") String id) {
        Map<String, Object> response = new HashMap<>();

        try {
            int userId = Integer.parseInt(id);
            User user = users.stream()
                    .filter(u -> u.getUserId() == userId)
                    .findFirst()
                    .orElse(null);

            if (user == null) {
                response.put("success", false);
                response.put("message", "Пользователь не найден");
                return response;
            }

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userId", user.getUserId());
            userMap.put("fullName", user.getFullName());
            userMap.put("firstName", user.getFirstName());
            userMap.put("lastName", user.getLastName());
            userMap.put("middleName", user.getMiddleName());
            userMap.put("email", user.getEmail());
            userMap.put("phone", user.getPhone());
            userMap.put("birthDate", user.getBirthDate() != null ? user.getBirthDate().toString() : null);
            userMap.put("login", user.getLogin());
            userMap.put("info", user.getInfo());
            userMap.put("role", user.getRole() != null ? user.getRole().getRoleName() : "Неизвестно");

            response.put("success", true);
            response.put("user", userMap);

        } catch (NumberFormatException e) {
            response.put("success", false);
            response.put("message", "Некорректный формат ID");
        }

        return response;
    }

    // Страница создания пользователя
    @GetMapping("/create")
    public String create(Model model) {
        UserCreateViewModel viewModel = new UserCreateViewModel();

        // Заполняем списки для формы
        List<SelectListItem> availableRoles = roles.stream()
                .map(r -> new SelectListItem(String.valueOf(r.getRoleId()), r.getRoleName()))
                .collect(Collectors.toList());

        List<SelectListItem> schoolClassesList = schoolClasses.stream()
                .map(c -> new SelectListItem(String.valueOf(c.getClassId()), c.getClassName()))
                .collect(Collectors.toList());

        // Список учеников для выбора родителя
        List<SelectListItem> allStudents = users.stream()
                .filter(u -> u.getRole() != null && "Ученик".equals(u.getRole().getRoleName()))
                .map(u -> new SelectListItem(String.valueOf(u.getUserId()), u.getFullName()))
                .collect(Collectors.toList());

        viewModel.setAvailableRoles(availableRoles);
        viewModel.setSchoolClasses(schoolClassesList);
        viewModel.setAllStudents(allStudents);

        model.addAttribute("viewModel", viewModel);
        model.addAttribute("title", "Создание пользователя");
        model.addAttribute("activePage", "users");
        model.addAttribute("content", "users/create");

        return "layout";
    }

    // Создание пользователя (POST)
    @PostMapping("/create")
    public String createUser(@ModelAttribute UserCreateViewModel model,
                             RedirectAttributes redirectAttributes) {
        try {
            // Генерация логина и пароля
            String login = generateLogin(model.getFirstName(), model.getLastName());
            String password = generateRandomPassword(6);

            // Создание нового пользователя
            User newUser = new User();
            newUser.setUserId(users.size() + 1);
            newUser.setLogin(login);
            newUser.setFirstName(model.getFirstName());
            newUser.setLastName(model.getLastName());
            newUser.setMiddleName(model.getMiddleName());
            newUser.setEmail(model.getEmail());
            newUser.setPhone(model.getPhone());
            newUser.setBirthDate(model.getBirthDate());
            newUser.setInfo(model.getInfo());
            newUser.setCoins(0);

            // Устанавливаем роль
            int roleId = model.getRoleId();
            Role selectedRole = roles.stream()
                    .filter(r -> r.getRoleId() == roleId)
                    .findFirst()
                    .orElse(null);

            if (selectedRole != null) {
                newUser.setRole(selectedRole);
            }

            // Устанавливаем пароль (в реальном приложении нужно хэшировать)
            newUser.setPassword(password);

            users.add(newUser);

            // Добавляем сообщение об успехе
            String successMessage = String.format(
                    "Пользователь %s успешно создан с ролью %s. Временный пароль: %s",
                    newUser.getFullName(),
                    selectedRole != null ? selectedRole.getRoleName() : "не определена",
                    password
            );

            redirectAttributes.addFlashAttribute("successMessage", successMessage);

            return "redirect:/users/index";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка создания пользователя: " + e.getMessage());
            return "redirect:/users/create";
        }
    }

    // Редактирование пользователя (частичное представление для модального окна)
    @GetMapping("/edit-user-partial")
    @ResponseBody
    public Map<String, Object> editUserPartial(@RequestParam("id") int userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = users.stream()
                    .filter(u -> u.getUserId() == userId)
                    .findFirst()
                    .orElse(null);

            if (user == null) {
                response.put("success", false);
                response.put("message", "Пользователь не найден");
                return response;
            }

            // Создаем ViewModel для редактирования
            UserCreateViewModel viewModel = new UserCreateViewModel();
            viewModel.setUserId(user.getUserId());
            viewModel.setFirstName(user.getFirstName());
            viewModel.setLastName(user.getLastName());
            viewModel.setMiddleName(user.getMiddleName());
            viewModel.setEmail(user.getEmail());
            viewModel.setPhone(user.getPhone());
            viewModel.setBirthDate(user.getBirthDate());
            viewModel.setInfo(user.getInfo());
            viewModel.setRoleId(user.getRole() != null ? user.getRole().getRoleId() : 0);
            viewModel.setLogin(user.getLogin());

            // Заполняем списки
            List<SelectListItem> availableRoles = roles.stream()
                    .map(r -> new SelectListItem(String.valueOf(r.getRoleId()), r.getRoleName()))
                    .collect(Collectors.toList());

            List<SelectListItem> schoolClassesList = schoolClasses.stream()
                    .map(c -> new SelectListItem(String.valueOf(c.getClassId()), c.getClassName()))
                    .collect(Collectors.toList());

            List<SelectListItem> allStudents = users.stream()
                    .filter(u -> u.getRole() != null && "Ученик".equals(u.getRole().getRoleName()))
                    .map(u -> new SelectListItem(String.valueOf(u.getUserId()), u.getFullName()))
                    .collect(Collectors.toList());

            viewModel.setAvailableRoles(availableRoles);
            viewModel.setSchoolClasses(schoolClassesList);
            viewModel.setAllStudents(allStudents);

            response.put("success", true);
            response.put("viewModel", viewModel);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки: " + e.getMessage());
        }

        return response;
    }

    // Сохранение изменений пользователя
    @PostMapping("/edit-user")
    @ResponseBody
    public Map<String, Object> editUser(@RequestBody UserEditRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            User userToUpdate = users.stream()
                    .filter(u -> u.getUserId() == request.getUserId())
                    .findFirst()
                    .orElse(null);

            if (userToUpdate == null) {
                response.put("success", false);
                response.put("message", "Пользователь не найден");
                return response;
            }

            // Обновляем данные пользователя
            userToUpdate.setLastName(request.getLastName());
            userToUpdate.setFirstName(request.getFirstName());
            userToUpdate.setMiddleName(request.getMiddleName());
            userToUpdate.setEmail(request.getEmail());
            userToUpdate.setPhone(request.getPhone());
            userToUpdate.setBirthDate(request.getBirthDate());
            userToUpdate.setInfo(request.getInfo());

            response.put("success", true);
            response.put("message", "Данные пользователя успешно обновлены");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка обновления: " + e.getMessage());
        }

        return response;
    }

    // Удаление пользователя
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteUser(@RequestParam("id") int userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            User userToDelete = users.stream()
                    .filter(u -> u.getUserId() == userId)
                    .findFirst()
                    .orElse(null);

            if (userToDelete == null) {
                response.put("success", false);
                response.put("message", "Пользователь не найден");
                return response;
            }

            // Проверяем, можно ли удалить пользователя
            if (userToDelete.getRole() != null && "Директор".equals(userToDelete.getRole().getRoleName())) {
                response.put("success", false);
                response.put("message", "Невозможно удалить пользователя с ролью Директор");
                return response;
            }

            // Удаляем пользователя
            users.removeIf(u -> u.getUserId() == userId);

            response.put("success", true);
            response.put("message", "Пользователь успешно удален");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка удаления: " + e.getMessage());
        }

        return response;
    }

    // Управление классами (частичное представление)
    @GetMapping("/manage-classes-partial")
    @ResponseBody
    public Map<String, Object> manageClassesPartial() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Получаем список учителей для выбора классного руководителя
            List<SelectListItem> availableTeachers = users.stream()
                    .filter(u -> u.getRole() != null && "Учитель".equals(u.getRole().getRoleName()))
                    .map(u -> new SelectListItem(String.valueOf(u.getUserId()), u.getFullName()))
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("schoolClasses", schoolClasses);
            response.put("availableTeachers", availableTeachers);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки: " + e.getMessage());
        }

        return response;
    }

    // Добавление нового класса
    @PostMapping("/add-class")
    @ResponseBody
    public Map<String, Object> addClass(@RequestParam("classNumber") int classNumber,
                                        @RequestParam("classLetter") String classLetter,
                                        @RequestParam("classTeacherId") int classTeacherId) {
        Map<String, Object> response = new HashMap<>();

        try {
            final int finalClassNumber = classNumber;
            final String finalClassLetter = classLetter.toUpperCase();

            // Проверяем, существует ли уже такой класс
            boolean exists = schoolClasses.stream()
                    .anyMatch(c -> c.getClassNumber() == finalClassNumber &&
                            c.getClassLetter().equalsIgnoreCase(finalClassLetter));

            if (exists) {
                response.put("success", false);
                response.put("message", String.format("Класс %d%s уже существует",
                        finalClassNumber, finalClassLetter));
                return response;
            }

            // Создаем новый класс
            SchoolClass newClass = new SchoolClass();
            newClass.setClassId(schoolClasses.size() + 1);
            newClass.setClassNumber(finalClassNumber);
            newClass.setClassLetter(finalClassLetter);

            // Находим учителя по ID
            User teacher = users.stream()
                    .filter(u -> u.getUserId() == classTeacherId)
                    .findFirst()
                    .orElse(null);
            newClass.setClassTeacher(teacher);

            schoolClasses.add(newClass);

            // Получаем имя учителя
            String teacherName = teacher != null ? teacher.getFullName() : "Неизвестно";

            response.put("success", true);
            response.put("message", String.format("Класс %s успешно добавлен. Классный руководитель: %s",
                    newClass.getClassName(), teacherName));

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка добавления класса: " + e.getMessage());
        }

        return response;
    }

    // Удаление класса
    @PostMapping("/delete-class")
    @ResponseBody
    public Map<String, Object> deleteClass(@RequestParam("classId") int classId) {
        Map<String, Object> response = new HashMap<>();

        try {
            final int finalClassId = classId;

            SchoolClass classToDelete = schoolClasses.stream()
                    .filter(c -> c.getClassId() == finalClassId)
                    .findFirst()
                    .orElse(null);

            if (classToDelete == null) {
                response.put("success", false);
                response.put("message", "Класс не найден");
                return response;
            }

            schoolClasses.removeIf(c -> c.getClassId() == finalClassId);

            response.put("success", true);
            response.put("message", String.format("Класс %s успешно удален",
                    classToDelete.getClassName()));

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка удаления класса: " + e.getMessage());
        }

        return response;
    }

    // Управление предметами (частичное представление)
    @GetMapping("/manage-subjects-partial")
    @ResponseBody
    public Map<String, Object> manageSubjectsPartial() {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("success", true);
            response.put("subjects", subjects);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка загрузки: " + e.getMessage());
        }

        return response;
    }

    // Добавление нового предмета
    @PostMapping("/add-subject")
    @ResponseBody
    public Map<String, Object> addSubject(@RequestParam("subjectName") String subjectName) {
        Map<String, Object> response = new HashMap<>();

        try {
            final String finalSubjectName = subjectName;

            // Проверяем, существует ли уже такой предмет
            boolean exists = subjects.stream()
                    .anyMatch(s -> s.getSubjectName().equalsIgnoreCase(finalSubjectName));

            if (exists) {
                response.put("success", false);
                response.put("message", String.format("Предмет '%s' уже существует", finalSubjectName));
                return response;
            }

            // Создаем новый предмет
            Subject newSubject = new Subject(subjectName);
            newSubject.setSubjectId(subjects.size() + 1);

            subjects.add(newSubject);

            response.put("success", true);
            response.put("message", String.format("Предмет '%s' успешно добавлен", subjectName));

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка добавления предмета: " + e.getMessage());
        }

        return response;
    }

    // Удаление предмета
    @PostMapping("/delete-subject")
    @ResponseBody
    public Map<String, Object> deleteSubject(@RequestParam("subjectId") int subjectId) {
        Map<String, Object> response = new HashMap<>();

        try {
            final int finalSubjectId = subjectId;

            Subject subjectToDelete = subjects.stream()
                    .filter(s -> s.getSubjectId() == finalSubjectId)
                    .findFirst()
                    .orElse(null);

            if (subjectToDelete == null) {
                response.put("success", false);
                response.put("message", "Предмет не найден");
                return response;
            }

            subjects.removeIf(s -> s.getSubjectId() == finalSubjectId);

            response.put("success", true);
            response.put("message", String.format("Предмет '%s' успешно удален",
                    subjectToDelete.getSubjectName()));

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка удаления предмета: " + e.getMessage());
        }

        return response;
    }

    // Вспомогательные методы

    private String generateLogin(String firstName, String lastName) {
        if (firstName == null || lastName == null || firstName.isEmpty() || lastName.isEmpty()) {
            return "user" + (users.size() + 1);
        }

        String baseLogin = firstName.toLowerCase().charAt(0) +
                lastName.toLowerCase().replaceAll("[^a-zа-я]", "");

        String login = baseLogin;
        int counter = 1;

        final String finalBaseLogin = baseLogin;

        while (true) {
            final String currentLogin = (counter == 1) ? finalBaseLogin : finalBaseLogin + counter;
            final int currentCounter = counter;

            boolean exists = users.stream().anyMatch(u -> {
                String username = u.getLogin();
                return username != null && username.equals(currentLogin);
            });

            if (!exists) {
                login = currentLogin;
                break;
            }
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

    // Вспомогательные классы для ViewModel

    public static class UserCreateViewModel {
        private int userId;
        private String firstName;
        private String lastName;
        private String middleName;
        private String email;
        private String phone;
        private LocalDate birthDate;
        private String info;
        private int roleId;
        private String login;
        private Integer classId;
        private Integer studentIdForParent;
        private List<SelectListItem> availableRoles;
        private List<SelectListItem> schoolClasses;
        private List<SelectListItem> allStudents;

        // Геттеры и сеттеры
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

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

        public String getLogin() { return login; }
        public void setLogin(String login) { this.login = login; }

        public Integer getClassId() { return classId; }
        public void setClassId(Integer classId) { this.classId = classId; }

        public Integer getStudentIdForParent() { return studentIdForParent; }
        public void setStudentIdForParent(Integer studentIdForParent) { this.studentIdForParent = studentIdForParent; }

        public List<SelectListItem> getAvailableRoles() { return availableRoles; }
        public void setAvailableRoles(List<SelectListItem> availableRoles) { this.availableRoles = availableRoles; }

        public List<SelectListItem> getSchoolClasses() { return schoolClasses; }
        public void setSchoolClasses(List<SelectListItem> schoolClasses) { this.schoolClasses = schoolClasses; }

        public List<SelectListItem> getAllStudents() { return allStudents; }
        public void setAllStudents(List<SelectListItem> allStudents) { this.allStudents = allStudents; }
    }

    public static class UserEditRequest {
        private int userId;
        private String firstName;
        private String lastName;
        private String middleName;
        private String email;
        private String phone;
        private LocalDate birthDate;
        private String info;
        private Integer classId;
        private Integer studentIdForParent;
        private Integer teacherClassId;

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

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

        public Integer getClassId() { return classId; }
        public void setClassId(Integer classId) { this.classId = classId; }

        public Integer getStudentIdForParent() { return studentIdForParent; }
        public void setStudentIdForParent(Integer studentIdForParent) { this.studentIdForParent = studentIdForParent; }

        public Integer getTeacherClassId() { return teacherClassId; }
        public void setTeacherClassId(Integer teacherClassId) { this.teacherClassId = teacherClassId; }
    }

    public static class SelectListItem {
        private String value;
        private String text;

        public SelectListItem() {}

        public SelectListItem(String value, String text) {
            this.value = value;
            this.text = text;
        }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    // Вспомогательный метод для создания тестовых пользователей
    private User createMockUser(int id, String login, String first, String last, String middle,
                                Role role, String email, String phone, LocalDate birthDate, String info) {
        User u = new User();
        u.setUserId(id);
        u.setLogin(login);
        u.setFirstName(first);
        u.setLastName(last);
        u.setMiddleName(middle);
        u.setPassword("1234");
        u.setEmail(email);
        u.setPhone(phone);
        u.setBirthDate(birthDate);
        u.setInfo(info);
        u.setCoins(0);
        u.setRole(role);
        return u;
    }
}