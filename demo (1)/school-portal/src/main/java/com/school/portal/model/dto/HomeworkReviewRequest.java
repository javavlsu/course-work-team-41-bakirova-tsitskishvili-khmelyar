package com.school.portal.model.dto;

public class HomeworkReviewRequest {
    private Integer homeworkId;
    private Integer gradeValue;
    private String comment;
    private Integer existingGradeId;

    public Integer getHomeworkId() { return homeworkId; }
    public void setHomeworkId(Integer homeworkId) { this.homeworkId = homeworkId; }

    public Integer getGradeValue() { return gradeValue; }
    public void setGradeValue(Integer gradeValue) { this.gradeValue = gradeValue; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Integer getExistingGradeId() { return existingGradeId; }
    public void setExistingGradeId(Integer existingGradeId) { this.existingGradeId = existingGradeId; }
}