package com.school.portal.model;

public class HomeworkReviewRequest {
    private int homeworkId;
    private Integer gradeValue;
    private String comment;
    private Integer existingGradeId;

    // Геттеры и сеттеры
    public int getHomeworkId() { return homeworkId; }
    public void setHomeworkId(int homeworkId) { this.homeworkId = homeworkId; }

    public Integer getGradeValue() { return gradeValue; }
    public void setGradeValue(Integer gradeValue) { this.gradeValue = gradeValue; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Integer getExistingGradeId() { return existingGradeId; }
    public void setExistingGradeId(Integer existingGradeId) { this.existingGradeId = existingGradeId; }
}