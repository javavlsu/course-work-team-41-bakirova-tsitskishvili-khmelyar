package com.school.portal.model;

import java.util.List;
import java.util.Map;

public class HomeworkReviewViewModel {
    private List<HomeworkReviewItem> submissions;
    private Map<Integer, String> availableClasses;
    private Integer selectedClassId;
    private String selectedClassName;
    private String errorMessage;

    // Геттеры и сеттеры
    public List<HomeworkReviewItem> getSubmissions() { return submissions; }
    public void setSubmissions(List<HomeworkReviewItem> submissions) { this.submissions = submissions; }

    public Map<Integer, String> getAvailableClasses() { return availableClasses; }
    public void setAvailableClasses(Map<Integer, String> availableClasses) { this.availableClasses = availableClasses; }

    public Integer getSelectedClassId() { return selectedClassId; }
    public void setSelectedClassId(Integer selectedClassId) { this.selectedClassId = selectedClassId; }

    public String getSelectedClassName() { return selectedClassName; }
    public void setSelectedClassName(String selectedClassName) { this.selectedClassName = selectedClassName; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}