package com.school.portal.model.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HomeworkReviewItem {
    private Integer homeworkId;
    private Integer studentId;
    private String studentFullName;
    private Integer classId;
    private String className;
    private Integer subjectId;
    private String subjectName;
    private String lessonTopic;
    private String homeworkText;
    private LocalDateTime lessonDate;
    private Integer lessonNumber;
    private LocalDateTime submissionDate;
    private String studentAnswer;
    private Integer statusId;
    private String currentTeacherComment;
    private Integer gradeId;
    private Integer currentGradeValue;
    private String currentGradeComment;

    // Constructors
    public HomeworkReviewItem() {}

    // Getters
    public Integer getHomeworkId() { return homeworkId; }
    public Integer getStudentId() { return studentId; }
    public String getStudentFullName() { return studentFullName; }
    public Integer getClassId() { return classId; }
    public String getClassName() { return className; }
    public Integer getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    public String getLessonTopic() { return lessonTopic; }
    public String getHomeworkText() { return homeworkText; }
    public LocalDateTime getLessonDate() { return lessonDate; }
    public Integer getLessonNumber() { return lessonNumber; }
    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public String getStudentAnswer() { return studentAnswer; }
    public Integer getStatusId() { return statusId; }
    public String getCurrentTeacherComment() { return currentTeacherComment; }
    public Integer getGradeId() { return gradeId; }
    public Integer getCurrentGradeValue() { return currentGradeValue; }
    public String getCurrentGradeComment() { return currentGradeComment; }

    // Setters
    public void setHomeworkId(Integer homeworkId) { this.homeworkId = homeworkId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public void setClassName(String className) { this.className = className; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public void setLessonTopic(String lessonTopic) { this.lessonTopic = lessonTopic; }
    public void setHomeworkText(String homeworkText) { this.homeworkText = homeworkText; }
    public void setLessonDate(LocalDateTime lessonDate) { this.lessonDate = lessonDate; }
    public void setLessonNumber(Integer lessonNumber) { this.lessonNumber = lessonNumber; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }
    public void setCurrentTeacherComment(String currentTeacherComment) { this.currentTeacherComment = currentTeacherComment; }
    public void setGradeId(Integer gradeId) { this.gradeId = gradeId; }
    public void setCurrentGradeValue(Integer currentGradeValue) { this.currentGradeValue = currentGradeValue; }
    public void setCurrentGradeComment(String currentGradeComment) { this.currentGradeComment = currentGradeComment; }

    // Helper methods
    public String getFormattedLessonDate() {
        if (lessonDate == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return lessonDate.format(formatter);
    }

    public String getFormattedSubmissionDate() {
        if (submissionDate == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return submissionDate.format(formatter);
    }

    public String getReviewStatus() {
        if (statusId == null) return "Не сдано";
        switch (statusId) {
            case 0: return "Не сдано";
            case 1: return "Сдано";
            case 2: return "Проверено";
            default: return "Неизвестно";
        }
    }

    public String getStatusCssClass() {
        if (statusId == null) return "status-badge bg-secondary";
        switch (statusId) {
            case 0: return "status-badge bg-secondary";
            case 1: return "status-badge bg-warning";
            case 2: return "status-badge bg-success";
            default: return "status-badge bg-secondary";
        }
    }

    public String getShortLessonTopic() {
        if (lessonTopic == null || lessonTopic.isEmpty()) return "";
        if (lessonTopic.length() > 30) return lessonTopic.substring(0, 27) + "...";
        return lessonTopic;
    }

    public String getShortHomeworkText() {
        if (homeworkText == null || homeworkText.isEmpty()) return "";
        if (homeworkText.length() > 40) return homeworkText.substring(0, 37) + "...";
        return homeworkText;
    }

    public String getShortStudentAnswer() {
        if (studentAnswer == null || studentAnswer.isEmpty()) return "";
        if (studentAnswer.length() > 50) return studentAnswer.substring(0, 47) + "...";
        return studentAnswer;
    }
}