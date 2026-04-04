package com.school.portal.model.dto;

public class HomeworkReviewRequest {
    private Integer homeworkId;
    private Integer gradeValue;
    private String comment;
    private Integer existingGradeId;

    // Constructors
    public HomeworkReviewRequest() {}

    // Getters
    public Integer getHomeworkId() { return homeworkId; }
    public Integer getGradeValue() { return gradeValue; }
    public String getComment() { return comment; }
    public Integer getExistingGradeId() { return existingGradeId; }

    // Setters
    public void setHomeworkId(Integer homeworkId) { this.homeworkId = homeworkId; }
    public void setGradeValue(Integer gradeValue) { this.gradeValue = gradeValue; }
    public void setComment(String comment) { this.comment = comment; }
    public void setExistingGradeId(Integer existingGradeId) { this.existingGradeId = existingGradeId; }
}