package com.school.portal.model;

import java.time.LocalDateTime;

// Модель для урока/занятия
public class Schedule {
    private int lessonId;
    private int classId;
    private int subjectId;
    private LocalDateTime date;
    private String lessonTopic;
    private String homeworkText;
    private SchoolClass schoolClass;
    private Subject subject;


    // Конструкторы, геттеры и сеттеры
    public Schedule() {}

    public Schedule(int lessonId, int classId, int subjectId, LocalDateTime date, String lessonTopic, String homeworkText) {
        this.lessonId = lessonId;
        this.classId = classId;
        this.subjectId = subjectId;
        this.date = date;
        this.lessonTopic = lessonTopic;
        this.homeworkText = homeworkText;
    }

    public int getLessonId() { return lessonId; }
    public void setLessonId(int lessonId) { this.lessonId = lessonId; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getLessonTopic() { return lessonTopic; }
    public void setLessonTopic(String lessonTopic) { this.lessonTopic = lessonTopic; }

    public String getHomeworkText() { return homeworkText; }
    public void setHomeworkText(String homeworkText) { this.homeworkText = homeworkText; }

    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public String getFormattedDate() {
        if (date != null) {
            return String.format("%02d.%02d", date.getDayOfMonth(), date.getMonthValue());
        }
        return "";
    }

    public String getShortLessonTopic() {
        if (lessonTopic == null || lessonTopic.isEmpty()) {
            return "тема не указана";
        }
        return lessonTopic.length() > 30 ? lessonTopic.substring(0, 30) + "..." : lessonTopic;
    }
}