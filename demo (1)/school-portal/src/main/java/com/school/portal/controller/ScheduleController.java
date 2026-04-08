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
            LocalDateTime weekEnd = startOfWeek.plusDays(7).atTime(LocalTime.MAX);

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
        } else {
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
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ========================================================================

    @GetMapping("/get-lesson-details")
    @ResponseBody
    public Map<String, Object> getLessonDetails(@RequestParam Integer lessonId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Schedule lesson = scheduleRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Урок не найден"));

            Map<String, Object> lessonData = new HashMap<>();
            lessonData.put("lessonId", lesson.getLessonId());
            lessonData.put("subject", lesson.getSubject() != null ? lesson.getSubject().getSubjectName() : "Неизвестно");
            lessonData.put("date", lesson.getLessonDateTime().format(DATE_FORMATTER));
            lessonData.put("lessonNumber", getLessonNumberByTime(lesson.getLessonDateTime().toLocalTime()));
            lessonData.put("teacher", lesson.getTeacher() != null ? lesson.getTeacher().getFullName() : "Неизвестно");
            lessonData.put("classroom", lesson.getRoom());
            lessonData.put("lessonTopic", lesson.getLessonTopic());
            lessonData.put("homeworkText", lesson.getHomeworkText());

            response.put("success", true);
            response.put("data", lessonData);
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