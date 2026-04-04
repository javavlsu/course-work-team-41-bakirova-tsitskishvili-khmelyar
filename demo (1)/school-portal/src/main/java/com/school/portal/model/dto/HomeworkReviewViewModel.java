package com.school.portal.model.dto;

import java.util.List;
import java.util.Map;

public class HomeworkReviewViewModel {
    private List<HomeworkReviewItem> submissions;
    private Map<Integer, String> availableClasses;
    private Map<Integer, String> availableSubjects;
    private Integer selectedClassId;
    private Integer selectedSubjectId;
    private String selectedClassName;
    private String selectedSubjectName;
    private String errorMessage;

    // Constructors
    public HomeworkReviewViewModel() {}

    // Getters
    public List<HomeworkReviewItem> getSubmissions() { return submissions; }
    public Map<Integer, String> getAvailableClasses() { return availableClasses; }
    public Map<Integer, String> getAvailableSubjects() { return availableSubjects; }
    public Integer getSelectedClassId() { return selectedClassId; }
    public Integer getSelectedSubjectId() { return selectedSubjectId; }
    public String getSelectedClassName() { return selectedClassName; }
    public String getSelectedSubjectName() { return selectedSubjectName; }
    public String getErrorMessage() { return errorMessage; }

    // Setters
    public void setSubmissions(List<HomeworkReviewItem> submissions) { this.submissions = submissions; }
    public void setAvailableClasses(Map<Integer, String> availableClasses) { this.availableClasses = availableClasses; }
    public void setAvailableSubjects(Map<Integer, String> availableSubjects) { this.availableSubjects = availableSubjects; }
    public void setSelectedClassId(Integer selectedClassId) { this.selectedClassId = selectedClassId; }
    public void setSelectedSubjectId(Integer selectedSubjectId) { this.selectedSubjectId = selectedSubjectId; }
    public void setSelectedClassName(String selectedClassName) { this.selectedClassName = selectedClassName; }
    public void setSelectedSubjectName(String selectedSubjectName) { this.selectedSubjectName = selectedSubjectName; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}