package com.school.portal.model;

import java.time.LocalDateTime;

public class HomeworkReviewItem {
    private int homeworkId;
    private int studentId;
    private String studentFullName;
    private int classId;
    private String className;
    private String subjectName;
    private LocalDateTime lessonDate;
    private int lessonNumber;
    private LocalDateTime submissionDate;
    private String studentAnswer;
    private int statusId;
    private String currentTeacherComment;
    private Integer gradeId;
    private Integer currentGradeValue;

    // Геттеры и сеттеры
    public int getHomeworkId() { return homeworkId; }
    public void setHomeworkId(int homeworkId) { this.homeworkId = homeworkId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentFullName() { return studentFullName; }
    public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public LocalDateTime getLessonDate() { return lessonDate; }
    public void setLessonDate(LocalDateTime lessonDate) { this.lessonDate = lessonDate; }

    public int getLessonNumber() { return lessonNumber; }
    public void setLessonNumber(int lessonNumber) { this.lessonNumber = lessonNumber; }

    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }

    public String getCurrentTeacherComment() { return currentTeacherComment; }
    public void setCurrentTeacherComment(String currentTeacherComment) { this.currentTeacherComment = currentTeacherComment; }

    public Integer getGradeId() { return gradeId; }
    public void setGradeId(Integer gradeId) { this.gradeId = gradeId; }

    public Integer getCurrentGradeValue() { return currentGradeValue; }
    public void setCurrentGradeValue(Integer currentGradeValue) { this.currentGradeValue = currentGradeValue; }

    // Вспомогательные методы для Thymeleaf
    public String getReviewStatus() {
        switch (statusId) {
            case 0: return "Не сдано";
            case 1: return "Сдано";
            case 2: return "Проверено";
            default: return "Неизвестно";
        }
    }

    public String getStatusCssClass() {
        switch (statusId) {
            case 0: return "bg-red-100 text-red-800";
            case 1: return "bg-yellow-100 text-yellow-800";
            case 2: return "bg-green-100 text-green-800";
            default: return "bg-gray-100 text-gray-800";
        }
    }

    public String getFormattedLessonDate() {
        return lessonDate != null ?
                String.format("%02d.%02d", lessonDate.getDayOfMonth(), lessonDate.getMonthValue()) : "";
    }

    public String getFormattedSubmissionDate() {
        return submissionDate != null ?
                String.format("%02d.%02d.%04d %02d:%02d",
                        submissionDate.getDayOfMonth(),
                        submissionDate.getMonthValue(),
                        submissionDate.getYear(),
                        submissionDate.getHour(),
                        submissionDate.getMinute()) : "";
    }

    public String getShortStudentAnswer() {
        if (studentAnswer == null || studentAnswer.isEmpty()) {
            return "";
        }
        return studentAnswer.length() > 50 ?
                studentAnswer.substring(0, 50) + "..." : studentAnswer;
    }

    public String getShortTeacherComment() {
        if (currentTeacherComment == null || currentTeacherComment.isEmpty()) {
            return "Не задано";
        }
        return currentTeacherComment.length() > 50 ?
                currentTeacherComment.substring(0, 50) + "..." : currentTeacherComment;
    }
}