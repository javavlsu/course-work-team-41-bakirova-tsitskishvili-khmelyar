package com.school.portal.model.dto;

import java.util.List;
import java.util.Map;

public class HomeworkReviewViewModel {
    private List<HomeworkReviewItem> pendingSubmissions;  // Статус 1 - Сдано (на проверку)
    private List<HomeworkReviewItem> reviewedSubmissions; // Статус 2 - Проверено
    private Map<Integer, String> availableClasses;
    private Map<Integer, String> availableSubjects;
    private Integer selectedClassId;
    private Integer selectedSubjectId;
    private String selectedClassName;
    private String selectedSubjectName;
    private String errorMessage;
    private String activeTab; // active tab: "pending" or "reviewed"

    // Constructors
    public HomeworkReviewViewModel() {}

    // Getters
    public List<HomeworkReviewItem> getPendingSubmissions() { return pendingSubmissions; }
    public List<HomeworkReviewItem> getReviewedSubmissions() { return reviewedSubmissions; }
    public Map<Integer, String> getAvailableClasses() { return availableClasses; }
    public Map<Integer, String> getAvailableSubjects() { return availableSubjects; }
    public Integer getSelectedClassId() { return selectedClassId; }
    public Integer getSelectedSubjectId() { return selectedSubjectId; }
    public String getSelectedClassName() { return selectedClassName; }
    public String getSelectedSubjectName() { return selectedSubjectName; }
    public String getErrorMessage() { return errorMessage; }
    public String getActiveTab() { return activeTab; }

    // Setters
    public void setPendingSubmissions(List<HomeworkReviewItem> pendingSubmissions) { this.pendingSubmissions = pendingSubmissions; }
    public void setReviewedSubmissions(List<HomeworkReviewItem> reviewedSubmissions) { this.reviewedSubmissions = reviewedSubmissions; }
    public void setAvailableClasses(Map<Integer, String> availableClasses) { this.availableClasses = availableClasses; }
    public void setAvailableSubjects(Map<Integer, String> availableSubjects) { this.availableSubjects = availableSubjects; }
    public void setSelectedClassId(Integer selectedClassId) { this.selectedClassId = selectedClassId; }
    public void setSelectedSubjectId(Integer selectedSubjectId) { this.selectedSubjectId = selectedSubjectId; }
    public void setSelectedClassName(String selectedClassName) { this.selectedClassName = selectedClassName; }
    public void setSelectedSubjectName(String selectedSubjectName) { this.selectedSubjectName = selectedSubjectName; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setActiveTab(String activeTab) { this.activeTab = activeTab; }

    // Helper methods
    public int getPendingCount() {
        return pendingSubmissions != null ? pendingSubmissions.size() : 0;
    }

    public int getReviewedCount() {
        return reviewedSubmissions != null ? reviewedSubmissions.size() : 0;
    }
}