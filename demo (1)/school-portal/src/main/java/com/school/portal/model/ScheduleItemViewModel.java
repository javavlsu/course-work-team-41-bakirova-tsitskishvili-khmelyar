package com.school.portal.model;

import java.time.LocalDateTime;

public class ScheduleItemViewModel {
    private Integer scheduleId;
    private Integer lessonNumber;
    private String lessonTime;
    private LocalDateTime date;
    private Integer subjectId;
    private String subjectName;
    private Integer teacherId;
    private String teacherFullName;
    private String classroom;
    private String lessonTopic;
    private String homeworkText;
    private Integer grade;
    private String gradeComment;

    // Геттеры и сеттеры
    public Integer getScheduleId() { return scheduleId; }
    public void setScheduleId(Integer scheduleId) { this.scheduleId = scheduleId; }

    public Integer getLessonNumber() { return lessonNumber; }
    public void setLessonNumber(Integer lessonNumber) { this.lessonNumber = lessonNumber; }

    public String getLessonTime() { return lessonTime; }
    public void setLessonTime(String lessonTime) { this.lessonTime = lessonTime; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }

    public String getTeacherFullName() { return teacherFullName; }
    public void setTeacherFullName(String teacherFullName) { this.teacherFullName = teacherFullName; }

    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }

    public String getLessonTopic() { return lessonTopic; }
    public void setLessonTopic(String lessonTopic) { this.lessonTopic = lessonTopic; }

    public String getHomeworkText() { return homeworkText; }
    public void setHomeworkText(String homeworkText) { this.homeworkText = homeworkText; }

    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }

    public String getGradeComment() { return gradeComment; }
    public void setGradeComment(String gradeComment) { this.gradeComment = gradeComment; }
}