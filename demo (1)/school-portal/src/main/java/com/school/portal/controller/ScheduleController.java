package com.school.portal.controller;

import com.school.portal.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {

    // Статический словарь времени уроков
    private static final Map<Integer, String> LESSON_TIME_MAP = new HashMap<>();
    static {
        LESSON_TIME_MAP.put(1, "08:30 - 10:00");
        LESSON_TIME_MAP.put(2, "10:10 - 11:40");
        LESSON_TIME_MAP.put(3, "11:50 - 13:20");
        LESSON_TIME_MAP.put(4, "14:00 - 15:30");
        LESSON_TIME_MAP.put(5, "15:40 - 17:10");
        LESSON_TIME_MAP.put(6, "17:20 - 18:50");
    }

    // Форматтер для дат
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @GetMapping("/index")
    public String index(
            @RequestParam(value = "date", required = false) String dateStr,
            Model model) {

        LocalDate selectedDate = LocalDate.now();
        LocalDate startOfWeek = selectedDate;

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            String role = getCurrentUserRole(auth);

            if (username == null || "anonymousUser".equals(username) || role == null) {
                return "redirect:/login";
            }

            // Устанавливаем заголовок и активную страницу для сайдбара
            model.addAttribute("title", "Расписание");
            model.addAttribute("activePage", "schedule");
            model.addAttribute("content", "schedule/personal-view");

            // Определение выбранной даты
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    selectedDate = LocalDate.parse(dateStr);
                } catch (Exception e) {
                    selectedDate = LocalDate.now();
                }
            }

            // Расчет начала недели (понедельник)
            startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            boolean isParent = "ROLE_PARENT".equals(role);

            // Данные для представления
            String studentName = "Иванов Алексей Петрович";
            String className = "9 \"А\"";
            String classTeacher = "Петрова Мария Сергеевна";

            model.addAttribute("isParent", isParent);
            model.addAttribute("studentName", studentName);
            model.addAttribute("className", className);
            model.addAttribute("classTeacher", classTeacher);
            model.addAttribute("lessonTimes", LESSON_TIME_MAP);

            // Создаем демо-расписание
            List<ScheduleItemViewModel> demoLessons = createDemoLessons(startOfWeek);
            Map<DayOfWeek, List<ScheduleItemViewModel>> scheduleByDay = groupLessonsByDay(demoLessons);

            // Создаем ViewModel
            ScheduleViewModel viewModel = new ScheduleViewModel();
            viewModel.setScheduleByDay(scheduleByDay);
            viewModel.setSelectedDate(selectedDate);
            viewModel.setStartOfWeek(startOfWeek);

            model.addAttribute("viewModel", viewModel);
            model.addAttribute("currentDate", LocalDate.now().format(DATE_FORMATTER));

        } catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute("errorMessage", "Произошла ошибка при загрузке расписания.");
        }

        return "layout";
    }

    @GetMapping("/get-lesson-details")
    @ResponseBody
    public Map<String, Object> getLessonDetails(@RequestParam Integer lessonId) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (lessonId == null || lessonId <= 0) {
                response.put("success", false);
                response.put("message", "Неверный ID урока");
                return response;
            }

            // Здесь должна быть логика получения данных урока из БД
            // Временно используем демо-данные
            Map<String, Object> lessonData = createDemoLessonData(lessonId);

            response.put("success", true);
            response.put("data", lessonData);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при загрузке данных урока: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/submit-homework")
    @ResponseBody
    public Map<String, Object> submitHomework(
            @RequestParam Integer lessonId,
            @RequestParam String studentAnswer,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            String role = getCurrentUserRole(authentication);

            if (!"ROLE_STUDENT".equals(role)) {
                response.put("success", false);
                response.put("message", "Только ученики могут отправлять домашнее задание.");
                return response;
            }

            if (lessonId == null || lessonId == 0) {
                response.put("success", false);
                response.put("message", "Не удалось определить урок.");
                return response;
            }

            String safeAnswer = studentAnswer != null ? studentAnswer.trim() : "";
            if (safeAnswer.isEmpty()) {
                response.put("success", false);
                response.put("message", "Пожалуйста, введите текст ответа.");
                return response;
            }

            // Здесь должна быть логика сохранения домашнего задания в БД
            System.out.println("Сохранение ДЗ для урока " + lessonId + ": " + safeAnswer);

            response.put("success", true);
            response.put("message", "Домашнее задание успешно отправлено!");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Непредвиденная ошибка сервера: " + e.getMessage());
        }

        return response;
    }

    // Вспомогательные методы

    private String getCurrentUserRole(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "ROLE_ANONYMOUS";
        }

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");
    }

    private List<ScheduleItemViewModel> createDemoLessons(LocalDate startOfWeek) {
        List<ScheduleItemViewModel> lessons = new ArrayList<>();
        Random random = new Random(42);

        String[] subjects = {"Математика", "Русский язык", "Физика", "Химия", "История", "Английский язык", "Биология", "География", "Литература", "Информатика"};
        String[] teachers = {"Иванов А.П.", "Петрова М.С.", "Сидоров Д.И.", "Кузнецова А.В.", "Смирнов П.А.", "Федорова О.Н."};
        String[] classrooms = {"201", "205", "301", "304", "401", "402", "303", "202"};

        // Дни недели: понедельник - суббота
        DayOfWeek[] days = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY};

        for (int dayIndex = 0; dayIndex < days.length; dayIndex++) {
            DayOfWeek day = days[dayIndex];
            LocalDate currentDate = startOfWeek.plusDays(dayIndex);

            // Разное количество уроков в разные дни
            int lessonsPerDay;
            if (dayIndex < 5) { // Пн-Пт
                lessonsPerDay = dayIndex == 0 ? 5 : (dayIndex == 4 ? 4 : 6); // Понедельник - 5, Пятница - 4, остальные - 6
            } else { // Суббота
                lessonsPerDay = 3;
            }

            for (int lessonNum = 1; lessonNum <= lessonsPerDay; lessonNum++) {
                ScheduleItemViewModel lesson = new ScheduleItemViewModel();
                int lessonId = dayIndex * 10 + lessonNum;
                lesson.setScheduleId(lessonId);
                lesson.setLessonNumber(lessonNum);
                lesson.setDate(LocalDateTime.of(currentDate, java.time.LocalTime.of(8, 30).plusHours((lessonNum - 1) * 2)));

                int subjectIndex = (dayIndex * 3 + lessonNum) % subjects.length;
                int teacherIndex = (dayIndex + lessonNum) % teachers.length;
                int classroomIndex = (dayIndex * 2 + lessonNum) % classrooms.length;

                lesson.setSubjectName(subjects[subjectIndex]);
                lesson.setTeacherFullName(teachers[teacherIndex]);
                lesson.setClassroom(classrooms[classroomIndex]);

                // 60% вероятность наличия темы урока
                if (random.nextDouble() < 0.6) {
                    String[] topics = {
                            "Основные понятия",
                            "Решение задач",
                            "Теоретический материал",
                            "Практическая работа",
                            "Лабораторная работа",
                            "Контрольная работа"
                    };
                    lesson.setLessonTopic(subjects[subjectIndex] + ": " + topics[random.nextInt(topics.length)]);
                }

                // 70% вероятность наличия домашнего задания
                if (random.nextDouble() < 0.7) {
                    String[] homeworkTypes = {
                            "Учебник: стр. " + (20 + dayIndex * 5) + "-" + (25 + dayIndex * 5) + ", №" + (1 + lessonNum * 3),
                            "Подготовить доклад на тему",
                            "Решить задачи из рабочей тетради",
                            "Повторить теоретический материал",
                            "Подготовиться к контрольной работе",
                            "Выполнить практическое задание"
                    };
                    lesson.setHomeworkText(homeworkTypes[random.nextInt(homeworkTypes.length)]);
                }

                // 40% вероятность наличия оценки
                if (random.nextDouble() < 0.4) {
                    int grade = 2 + random.nextInt(4); // Оценки 2-5
                    lesson.setGrade(grade);

                    // Комментарии в зависимости от оценки
                    if (grade >= 4) {
                        lesson.setGradeComment("Отличная работа!");
                    } else if (grade == 3) {
                        lesson.setGradeComment("Можно лучше");
                    } else {
                        lesson.setGradeComment("Требуется дополнительная работа");
                    }
                }

                lessons.add(lesson);
            }
        }

        return lessons;
    }

    private Map<DayOfWeek, List<ScheduleItemViewModel>> groupLessonsByDay(List<ScheduleItemViewModel> lessons) {
        Map<DayOfWeek, List<ScheduleItemViewModel>> scheduleByDay = new EnumMap<>(DayOfWeek.class);

        // Инициализируем все дни недели
        for (DayOfWeek day : DayOfWeek.values()) {
            scheduleByDay.put(day, new ArrayList<>());
        }

        // Группируем уроки по дням недели
        for (ScheduleItemViewModel lesson : lessons) {
            DayOfWeek day = lesson.getDate().getDayOfWeek();
            scheduleByDay.get(day).add(lesson);
        }

        // Сортируем уроки по номеру урока
        for (List<ScheduleItemViewModel> dayLessons : scheduleByDay.values()) {
            dayLessons.sort(Comparator.comparingInt(ScheduleItemViewModel::getLessonNumber));
        }

        return scheduleByDay;
    }

    private Map<String, Object> createDemoLessonData(Integer lessonId) {
        Map<String, Object> lessonData = new HashMap<>();

        // Генерируем демо-данные на основе ID урока
        String[] subjects = {"Математика", "Русский язык", "Физика", "Химия", "История", "Английский язык", "Биология", "География"};
        String[] teachers = {"Иванов А.П.", "Петрова М.С.", "Сидоров Д.И.", "Кузнецова А.В.", "Смирнов П.А."};
        String[] topics = {
                "Решение квадратных уравнений",
                "Части речи в русском языке",
                "Законы Ньютона",
                "Химические реакции",
                "Великая Отечественная война",
                "Времена английского глагола",
                "Строение клетки",
                "Географические открытия"
        };

        String[] homework = {
                "Учебник: стр. 45-48, № 12-18",
                "Упражнения 1-5 на стр. 67",
                "Задачи 1-3 на стр. 89",
                "Лабораторная работа №3",
                "Подготовить доклад по теме",
                "Выучить новые слова, стр. 34",
                "Подготовить презентацию",
                "Нанести на контурную карту"
        };

        int index = lessonId % subjects.length;

        lessonData.put("lessonId", lessonId);
        lessonData.put("subject", subjects[index]);
        lessonData.put("date", LocalDate.now().minusDays(lessonId % 7).format(DATE_FORMATTER));
        lessonData.put("lessonNumber", (lessonId % 6) + 1);
        lessonData.put("teacher", teachers[index % teachers.length]);
        lessonData.put("lessonTopic", topics[index]);
        lessonData.put("homeworkText", homework[index]);

        return lessonData;
    }
}