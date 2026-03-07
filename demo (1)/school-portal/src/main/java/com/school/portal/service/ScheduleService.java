package com.school.portal.service;

import com.school.portal.model.Schedule;
import com.school.portal.model.ScheduleTemplate;
import com.school.portal.repository.ScheduleRepository;
import com.school.portal.repository.ScheduleTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleTemplateRepository templateRepository;

    private static final java.util.Map<Integer, LocalTime> LESSON_TIMES = new java.util.HashMap<>();
    static {
        LESSON_TIMES.put(1, LocalTime.of(8, 30));
        LESSON_TIMES.put(2, LocalTime.of(10, 10));
        LESSON_TIMES.put(3, LocalTime.of(11, 50));
        LESSON_TIMES.put(4, LocalTime.of(14, 0));
        LESSON_TIMES.put(5, LocalTime.of(15, 40));
        LESSON_TIMES.put(6, LocalTime.of(17, 20));
    }

    public List<Schedule> getLessonsForClassAndWeek(Integer classId, LocalDate weekStart) {
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekStart.plusDays(7).atTime(LocalTime.MAX);

        return scheduleRepository.findLessonsForClassBetween(classId, start, end);
    }

    public List<Schedule> getLessonsForTeacherAndWeek(Integer teacherId, LocalDate weekStart) {
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekStart.plusDays(7).atTime(LocalTime.MAX);

        return scheduleRepository.findLessonsForTeacherBetween(teacherId, start, end);
    }

    public List<Schedule> getLessonsForWeek(LocalDate weekStart) {
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekStart.plusDays(7).atTime(LocalTime.MAX);

        return scheduleRepository.findLessonsBetween(start, end);
    }

    @Transactional
    public Schedule createLesson(Schedule lesson) {
        return scheduleRepository.save(lesson);
    }

    @Transactional
    public Schedule updateLesson(Schedule lesson) {
        return scheduleRepository.save(lesson);
    }

    @Transactional
    public void deleteLesson(Integer lessonId) {
        scheduleRepository.deleteById(lessonId);
    }

    @Transactional
    public void generateLessonsFromTemplate(Integer classId, LocalDate startDate, LocalDate endDate) {
        List<ScheduleTemplate> templates = templateRepository.findBySchoolClassClassId(classId);
        List<Schedule> lessons = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            byte dayByte = (byte) dayOfWeek.getValue(); // 1-пн, 7-вс

            for (ScheduleTemplate template : templates) {
                if (template.getDayOfWeek() == dayByte) {
                    Schedule lesson = new Schedule();
                    lesson.setSchoolClass(template.getSchoolClass());
                    lesson.setSubject(template.getSubject());
                    lesson.setTeacher(template.getTeacher());
                    lesson.setRoom(template.getRoom());

                    // Устанавливаем время урока
                    LocalTime lessonTime = LESSON_TIMES.get(template.getLessonNumber());
                    if (lessonTime == null) lessonTime = LocalTime.of(8, 30);

                    lesson.setLessonDateTime(LocalDateTime.of(currentDate, lessonTime));

                    lessons.add(lesson);
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        scheduleRepository.saveAll(lessons);
    }

    public static String getLessonTimeRange(int lessonNumber) {
        LocalTime start = LESSON_TIMES.get(lessonNumber);
        if (start == null) return "";

        LocalTime end = start.plusMinutes(90);
        return String.format("%02d:%02d - %02d:%02d",
                start.getHour(), start.getMinute(),
                end.getHour(), end.getMinute());
    }
}