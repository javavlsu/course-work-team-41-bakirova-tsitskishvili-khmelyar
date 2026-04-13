package com.school.portal.controller;

import com.school.portal.model.*;
import com.school.portal.model.dto.ScheduleItemViewModel;
import com.school.portal.model.dto.ScheduleViewModel;
import com.school.portal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ClassSubjectTeacherRepository classSubjectTeacherRepository;

    @Autowired
    private HomeworkRepository homeworkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private SchoolClassRepository classRepository;

    @Autowired
    private StudentClassRepository studentClassRepository;

    @Autowired
    private GradeRepository gradeRepository;

    private static final Map<Integer, String> LESSON_TIME_MAP = new HashMap<>();
    static {
        LESSON_TIME_MAP.put(1, "08:30 - 10:00");
        LESSON_TIME_MAP.put(2, "10:10 - 11:40");
        LESSON_TIME_MAP.put(3, "11:50 - 13:20");
        LESSON_TIME_MAP.put(4, "14:00 - 15:30");
        LESSON_TIME_MAP.put(5, "15:40 - 17:10");
        LESSON_TIME_MAP.put(6, "17:20 - 18:50");
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @GetMapping("/index")
    public String index(
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "filterType", required = false, defaultValue = "Class") String filterType,
            @RequestParam(value = "selectedId", required = false) Integer selectedId,
            Model model) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userRepository.findByLogin(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            String role = getCurrentUserRole(auth);

            // Определение дат
            LocalDate selectedDate = LocalDate.now();
            if (dateStr != null && !dateStr.isEmpty()) {
                try { selectedDate = LocalDate.parse(dateStr); } catch (Exception ignored) {}
            }
            LocalDate startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDateTime weekStart = startOfWeek.atStartOfDay();
            LocalDateTime weekEnd = startOfWeek.plusDays(6).atTime(LocalTime.MAX);

            // МАРШРУТИЗАЦИЯ В ЗАВИСИМОСТИ ОТ РОЛИ
            if ("ROLE_DIRECTOR".equals(role) || "ROLE_ADMIN".equals(role)) {
                return buildDirectorView(model, weekStart, weekEnd, selectedDate, startOfWeek, filterType, selectedId);
            } else {
                return buildPersonalView(model, currentUser, role, weekStart, weekEnd, selectedDate, startOfWeek);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute("errorMessage", "Произошла ошибка при загрузке расписания.");
            return "layout";
        }
    }

    @PostMapping("/submit-homework")
    @ResponseBody
    @Transactional
    public Map<String, Object> submitHomework(@RequestParam Integer lessonId, @RequestParam String studentAnswer) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User student = userRepository.findByLogin(auth.getName()).orElseThrow();
            Schedule lesson = scheduleRepository.findById(lessonId).orElseThrow();

            Homework homework = homeworkRepository.findByStudentUserIdAndLessonLessonId(student.getUserId(), lessonId)
                    .orElse(new Homework());

            // ЗАЩИТА: Если уже сдано (1) или проверено (2) - блокируем!
            if (homework.getStatus() != null && homework.getStatus() > 0) {
                response.put("success", false);
                response.put("message", "Вы уже отправили это задание. Повторная отправка запрещена.");
                return response;
            }

            homework.setStudent(student);
            homework.setLesson(lesson);
            homework.setText(studentAnswer);
            homework.setStatus(1); // 1 = Сдано!
            homework.setDate(LocalDateTime.now());
            homeworkRepository.save(homework);

            response.put("success", true);
            response.put("message", "Домашнее задание успешно отправлено!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/save-lesson-info")
    @ResponseBody
    @Transactional
    public Map<String, Object> saveLessonInfo(@RequestParam Integer lessonId,
                                              @RequestParam String topic,
                                              @RequestParam String homeworkText) {
        Map<String, Object> response = new HashMap<>();
        try {
            Schedule lesson = scheduleRepository.findById(lessonId).orElseThrow();

            lesson.setLessonTopic(topic);
            lesson.setHomeworkText(homeworkText);
            scheduleRepository.save(lesson);

            // Если учитель задал домашку, раздаем статус 0 всем ученикам класса
            if (homeworkText != null && !homeworkText.trim().isEmpty() && lesson.getSchoolClass() != null) {
                List<User> students = studentClassRepository.findStudentsByClassId(lesson.getSchoolClass().getClassId());
                for (User student : students) {
                    if (homeworkRepository.findByStudentUserIdAndLessonLessonId(student.getUserId(), lessonId).isEmpty()) {
                        Homework hw = new Homework();
                        hw.setLesson(lesson);
                        hw.setStudent(student);
                        hw.setText("");
                        hw.setStatus(0);
                        hw.setDate(LocalDateTime.now());
                        homeworkRepository.save(hw);
                    }
                }
            }

            response.put("success", true);
            response.put("message", "Информация об уроке и задание сохранены!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/save-task")
    @ResponseBody
    @Transactional
    public Map<String, Object> saveHomeworkTask(@RequestParam Integer lessonId, @RequestParam String taskText) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // Можно добавить проверку роли TEACHER/DIRECTOR для безопасности

            Schedule lesson = scheduleRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Урок не найден"));

            lesson.setHomeworkText(taskText); // Сохраняем само задание
            scheduleRepository.save(lesson);

            response.put("success", true);
            response.put("message", "Задание успешно сохранено!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }


    // ========================================================================
    // МЕТОД ДЛЯ ДИРЕКТОРА (ПОЛНОЕ РАСПИСАНИЕ С ФИЛЬТРАМИ)
    // ========================================================================
    private String buildDirectorView(Model model, LocalDateTime weekStart, LocalDateTime weekEnd,
                                     LocalDate selectedDate, LocalDate startOfWeek,
                                     String filterType, Integer selectedId) {

        ScheduleViewModel viewModel = new ScheduleViewModel();
        viewModel.setSelectedDate(selectedDate);
        viewModel.setStartOfWeek(startOfWeek);
        viewModel.setAdminView(true);
        viewModel.setPersonalView(false);
        viewModel.setFilterType(filterType);

        List<User> teachers = userRepository.findByRole_RoleName("TEACHER");
        List<SchoolClass> classes = classRepository.findAll();
        viewModel.setAvailableTeachers(teachers);
        viewModel.setAvailableClasses(classes);

        List<Schedule> lessons = new ArrayList<>();

        if ("Teacher".equals(filterType)) {
            // ФИЛЬТР ПО УЧИТЕЛЮ
            if (selectedId == null && !teachers.isEmpty()) {
                selectedId = teachers.get(0).getUserId();
            }
            viewModel.setSelectedTeacherId(selectedId);

            if (selectedId != null) {
                lessons = scheduleRepository.findLessonsForTeacherBetween(selectedId, weekStart, weekEnd);
                User selectedTeacher = userRepository.findById(selectedId).orElse(null);
                if (selectedTeacher != null) {
                    viewModel.setSelectedClassName("Учитель: " + selectedTeacher.getFullName());
                }
            }
        } else if ("Class".equals(filterType)) {
            // ФИЛЬТР ПО КЛАССУ
            if (selectedId == null && !classes.isEmpty()) {
                selectedId = classes.get(0).getClassId();
            }
            viewModel.setSelectedClassId(selectedId);

            if (selectedId != null) {
                lessons = scheduleRepository.findLessonsForClassBetween(selectedId, weekStart, weekEnd);
                SchoolClass selectedClass = classRepository.findById(selectedId).orElse(null);
                if (selectedClass != null) {
                    viewModel.setSelectedClassName("Класс: " + selectedClass.getClassName());
                }
            }
        } else {
            // ФИЛЬТР: ВСЯ ШКОЛА (БЕЗ ФИЛЬТРОВ)
            viewModel.setFilterType("All");
            viewModel.setSelectedClassName("Расписание всей школы");
            // Достаем вообще все уроки за эту неделю!
            lessons = scheduleRepository.findLessonsBetween(weekStart, weekEnd);
        }

        List<ScheduleItemViewModel> lessonViewModels = lessons.stream()
                .map(this::convertToScheduleItem)
                .collect(Collectors.toList());

        viewModel.setScheduleByDay(groupLessonsByDay(lessonViewModels));

        model.addAttribute("viewModel", viewModel);
        model.addAttribute("lessonTimes", LESSON_TIME_MAP);
        model.addAttribute("title", "Администрирование расписания");
        model.addAttribute("activePage", "schedule");
        model.addAttribute("content", "schedule/director-view");

        return "layout";
    }

    // ========================================================================
    // МЕТОД ДЛЯ УЧЕНИКОВ, РОДИТЕЛЕЙ И УЧИТЕЛЕЙ (ЛИЧНОЕ РАСПИСАНИЕ)
    // ========================================================================
    private String buildPersonalView(Model model, User currentUser, String role,
                                     LocalDateTime weekStart, LocalDateTime weekEnd,
                                     LocalDate selectedDate, LocalDate startOfWeek) {

        boolean isParent = "ROLE_PARENT".equals(role);
        List<Schedule> lessons = new ArrayList<>();

        if (isParent) {
            User student = userRepository.findByRole_RoleName("STUDENT").stream().findFirst().orElse(null);
            if (student != null) {
                Optional<StudentClass> sc = studentClassRepository.findByStudentUserId(student.getUserId());
                if (sc.isPresent()) {
                    lessons = scheduleRepository.findLessonsForClassBetween(
                            sc.get().getSchoolClass().getClassId(), weekStart, weekEnd);
                }
            }
        } else if ("ROLE_STUDENT".equals(role)) {
            Optional<StudentClass> sc = studentClassRepository.findByStudentUserId(currentUser.getUserId());
            if (sc.isPresent()) {
                lessons = scheduleRepository.findLessonsForClassBetween(
                        sc.get().getSchoolClass().getClassId(), weekStart, weekEnd);
            }
        } else if ("ROLE_TEACHER".equals(role)) {
            lessons = scheduleRepository.findLessonsForTeacherBetween(currentUser.getUserId(), weekStart, weekEnd);
        }

        List<ScheduleItemViewModel> lessonViewModels = lessons.stream()
                .map(this::convertToScheduleItem)
                .collect(Collectors.toList());

        ScheduleViewModel viewModel = new ScheduleViewModel();
        viewModel.setScheduleByDay(groupLessonsByDay(lessonViewModels));
        viewModel.setSelectedDate(selectedDate);
        viewModel.setStartOfWeek(startOfWeek);
        viewModel.setPersonalView(!isParent);
        viewModel.setAdminView(false);

        model.addAttribute("viewModel", viewModel);
        model.addAttribute("isParent", isParent);
        model.addAttribute("lessonTimes", LESSON_TIME_MAP);
        model.addAttribute("currentDate", LocalDate.now().format(DATE_FORMATTER));
        model.addAttribute("title", "Расписание");
        model.addAttribute("activePage", "schedule");
        model.addAttribute("content", "schedule/personal-view");

        return "layout";
    }

    // ========================================================================
    // АДМИНСКИЕ/ДИРЕКТОРСКИЕ МЕТОДЫ
    // ========================================================================

    // 1. Получение доступных предметов и учителей для выбранного класса
    @GetMapping("/get-class-mappings")
    @ResponseBody
    public Map<String, Object> getClassMappings(@RequestParam Integer classId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Ищем привязки для класса
            List<ClassSubjectTeacher> mappings = classSubjectTeacherRepository.findBySchoolClass_ClassId(classId);

            List<Map<String, Object>> mappingsData = new ArrayList<>();
            for (ClassSubjectTeacher mapping : mappings) {
                Map<String, Object> data = new HashMap<>();
                data.put("subjectId", mapping.getSubject().getSubjectId());
                data.put("subjectName", mapping.getSubject().getSubjectName());
                data.put("teacherId", mapping.getTeacher().getUserId());
                data.put("teacherName", mapping.getTeacher().getFullName());
                mappingsData.add(data);
            }

            response.put("success", true);
            response.put("mappings", mappingsData);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // А это тот же метод для выбранного учителя
    @GetMapping("/get-teacher-mappings")
    @ResponseBody
    public Map<String, Object> getTeacherMappings(@RequestParam Integer teacherId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ClassSubjectTeacher> mappings = classSubjectTeacherRepository.findByTeacher_UserId(teacherId);

            List<Map<String, Object>> mappingsData = new ArrayList<>();
            for (ClassSubjectTeacher mapping : mappings) {
                Map<String, Object> data = new HashMap<>();
                data.put("subjectId", mapping.getSubject().getSubjectId());
                data.put("subjectName", mapping.getSubject().getSubjectName());
                data.put("classId", mapping.getSchoolClass().getClassId());
                data.put("className", mapping.getSchoolClass().getClassName());
                mappingsData.add(data);
            }

            response.put("success", true);
            response.put("mappings", mappingsData);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // 2. Сохранение или создание нового урока
    @PostMapping("/save-admin-lesson")
    @ResponseBody
    @Transactional
    public Map<String, Object> saveAdminLesson(
            @RequestParam(required = false) Integer lessonId,
            @RequestParam Integer classId,
            @RequestParam Integer subjectId,
            @RequestParam Integer teacherId,
            @RequestParam String room,
            @RequestParam String dateStr,
            @RequestParam Integer lessonNum) {

        Map<String, Object> response = new HashMap<>();
        try {
            // Рассчитываем точное время начала урока
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            LocalTime time;
            switch (lessonNum) {
                case 1: time = LocalTime.of(8, 30); break;
                case 2: time = LocalTime.of(10, 10); break;
                case 3: time = LocalTime.of(11, 50); break;
                case 4: time = LocalTime.of(14, 0); break;
                case 5: time = LocalTime.of(15, 40); break;
                default: time = LocalTime.of(17, 20); break;
            }
            LocalDateTime lessonDateTime = date.atTime(time);

            Schedule lesson;
            if (lessonId != null && lessonId > 0) {
                lesson = scheduleRepository.findById(lessonId)
                        .orElseThrow(() -> new RuntimeException("Урок не найден"));
            } else {
                lesson = new Schedule();
                lesson.setLessonDateTime(lessonDateTime);
            }

            lesson.setSchoolClass(classRepository.findById(classId).orElseThrow());
            lesson.setSubject(subjectRepository.findById(subjectId).orElseThrow());
            lesson.setTeacher(userRepository.findById(teacherId).orElseThrow());
            lesson.setRoom(room);

            scheduleRepository.save(lesson);

            response.put("success", true);
            response.put("message", "Урок успешно сохранен!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }
        return response;
    }

    // 3. Удаление урока
    @PostMapping("/delete-admin-lesson")
    @ResponseBody
    @Transactional
    public Map<String, Object> deleteAdminLesson(@RequestParam Integer lessonId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Удаляем урок (каскадно должны удалиться или отвязаться домашки/оценки,
            // зависит от настроек БД. Если выдаст ошибку ForeignKey, нужно будет сначала чистить их).
            scheduleRepository.deleteById(lessonId);
            response.put("success", true);
            response.put("message", "Урок удален!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Нельзя удалить урок, если по нему уже выставлены оценки или сдано ДЗ.");
        }
        return response;
    }

    // ========================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ========================================================================

    @GetMapping("/get-lesson-details")
    @ResponseBody
    public Map<String, Object> getLessonDetails(@RequestParam Integer lessonId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userRepository.findByLogin(auth.getName()).orElse(null);

            Schedule lesson = scheduleRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Урок не найден"));

            Map<String, Object> data = new HashMap<>();
            data.put("lessonId", lesson.getLessonId());
            data.put("subject", lesson.getSubject() != null ? lesson.getSubject().getSubjectName() : "Неизвестно");
            data.put("date", lesson.getLessonDateTime().format(DATE_FORMATTER));
            data.put("lessonNumber", getLessonNumberByTime(lesson.getLessonDateTime().toLocalTime()));
            data.put("teacher", lesson.getTeacher() != null ? lesson.getTeacher().getFullName() : "Неизвестно");
            data.put("classroom", lesson.getRoom());
            data.put("lessonTopic", lesson.getLessonTopic());
            data.put("homeworkText", lesson.getHomeworkText());

            // Определяем роль для фронтенда
            boolean isTeacher = currentUser != null && "TEACHER".equals(currentUser.getRole().getRoleName());
            boolean isStudent = currentUser != null && "STUDENT".equals(currentUser.getRole().getRoleName());

            data.put("isTeacher", isTeacher);
            data.put("isStudent", isStudent);

            // Если это ученик, проверяем статус его домашки
            if (isStudent) {
                homeworkRepository.findByStudentUserIdAndLessonLessonId(currentUser.getUserId(), lessonId)
                        .ifPresentOrElse(hw -> {
                            data.put("homeworkStatus", hw.getStatus()); // 0, 1 или 2
                            data.put("studentAnswer", hw.getText());
                        }, () -> {
                            data.put("homeworkStatus", 0);
                        });
            }

            response.put("success", true);
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }
        return response;
    }

    private ScheduleItemViewModel convertToScheduleItem(Schedule schedule) {
        ScheduleItemViewModel item = new ScheduleItemViewModel();
        item.setScheduleId(schedule.getLessonId());
        item.setDate(schedule.getLessonDateTime());
        item.setLessonNumber(getLessonNumberByTime(schedule.getLessonDateTime().toLocalTime()));
        item.setLessonTime(LESSON_TIME_MAP.get(item.getLessonNumber()));

        if (schedule.getSubject() != null) {
            item.setSubjectId(schedule.getSubject().getSubjectId());
            item.setSubjectName(schedule.getSubject().getSubjectName());
        }

        if (schedule.getTeacher() != null) {
            item.setTeacherId(schedule.getTeacher().getUserId());
            item.setTeacherFullName(schedule.getTeacher().getFullName());
        }

        if (schedule.getSchoolClass() != null) {
            item.setClassroom(schedule.getRoom());
            // НОВОЕ ПОЛЕ: Название класса (чтобы работало в director-view)
            item.setClassName(schedule.getSchoolClass().getClassName());

            item.setClassId(schedule.getSchoolClass().getClassId());
        }

        item.setLessonTopic(schedule.getLessonTopic());
        item.setHomeworkText(schedule.getHomeworkText());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            userRepository.findByLogin(auth.getName()).ifPresent(user -> {
                if ("ROLE_STUDENT".equals(getCurrentUserRole(auth))) {
                    gradeRepository.findByStudentUserIdAndLessonLessonId(
                            user.getUserId(), schedule.getLessonId()).ifPresent(grade -> {
                        item.setGrade(grade.getGradeValue());
                        item.setGradeComment(grade.getComment());
                    });
                }
            });
        }
        return item;
    }

    private int getLessonNumberByTime(LocalTime time) {
        if (time.isBefore(LocalTime.of(9, 30))) return 1;
        if (time.isBefore(LocalTime.of(11, 0))) return 2;
        if (time.isBefore(LocalTime.of(12, 30))) return 3;
        if (time.isBefore(LocalTime.of(14, 30))) return 4;
        if (time.isBefore(LocalTime.of(16, 0))) return 5;
        return 6;
    }

    private Map<DayOfWeek, List<ScheduleItemViewModel>> groupLessonsByDay(List<ScheduleItemViewModel> lessons) {
        Map<DayOfWeek, List<ScheduleItemViewModel>> scheduleByDay = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            scheduleByDay.put(day, new ArrayList<>());
        }
        for (ScheduleItemViewModel lesson : lessons) {
            if (lesson.getDate() != null) {
                DayOfWeek day = lesson.getDate().getDayOfWeek();
                scheduleByDay.get(day).add(lesson);
            }
        }
        for (List<ScheduleItemViewModel> dayLessons : scheduleByDay.values()) {
            dayLessons.sort(Comparator.comparingInt(ScheduleItemViewModel::getLessonNumber));
        }
        return scheduleByDay;
    }

    private String getCurrentUserRole(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return "ROLE_ANONYMOUS";
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");
    }
}