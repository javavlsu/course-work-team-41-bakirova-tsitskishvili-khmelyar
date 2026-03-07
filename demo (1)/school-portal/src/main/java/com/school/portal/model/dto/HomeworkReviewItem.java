package com.school.portal.model.dto;

import java.time.LocalDateTime;

public class HomeworkReviewItem {
    private Integer homeworkId;
    private Integer studentId;
    private String studentFullName;
    private Integer classId;
    private String className;
    private String subjectName;
    private LocalDateTime lessonDate;
    private Integer lessonNumber;
    private LocalDateTime submissionDate;
    private String studentAnswer;
    private Integer statusId;
    private String currentTeacherComment;
    private Integer gradeId;
    private Integer currentGradeValue;

    public Integer getHomeworkId() { return homeworkId; }
    public void setHomeworkId(Integer homeworkId) { this.homeworkId = homeworkId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getStudentFullName() { return studentFullName; }
    public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public LocalDateTime getLessonDate() { return lessonDate; }
    public void setLessonDate(LocalDateTime lessonDate) { this.lessonDate = lessonDate; }

    public Integer getLessonNumber() { return lessonNumber; }
    public void setLessonNumber(Integer lessonNumber) { this.lessonNumber = lessonNumber; }

    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }

    public String getCurrentTeacherComment() { return currentTeacherComment; }
    public void setCurrentTeacherComment(String currentTeacherComment) { this.currentTeacherComment = currentTeacherComment; }

    public Integer getGradeId() { return gradeId; }
    public void setGradeId(Integer gradeId) { this.gradeId = gradeId; }

    public Integer getCurrentGradeValue() { return currentGradeValue; }
    public void setCurrentGradeValue(Integer currentGradeValue) { this.currentGradeValue = currentGradeValue; }

    public String getReviewStatus() {
        switch (statusId) {
            case 0: return "Не сдано";
            case 1: return "Сдано";
            case 2: return "Проверено";
            default: return "Неизвестно";
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
}