package com.school.portal.model.dto;

import java.util.List;
import java.util.Map;

public class HomeworkReviewViewModel {
    private List<HomeworkReviewItem> pendingSubmissions;
    private List<HomeworkReviewItem> reviewedSubmissions;
    private Map<Integer, String> availableClasses;
    private Map<Integer, String> availableSubjects;
    private Integer selectedClassId;
    private Integer selectedSubjectId;
    private String selectedClassName;
    private String selectedSubjectName;
    private String errorMessage;
    private String activeTab;

    // НОВЫЕ ПОЛЯ ДЛЯ ПАГИНАЦИИ И ФИЛЬТРАЦИИ
    private int currentPage;
    private int totalPages;
    private String search;
    private String dateFrom;
    private String dateTo;

    public HomeworkReviewViewModel() {}

    // Геттеры и сеттеры (старые)
    public List<HomeworkReviewItem> getPendingSubmissions() { return pendingSubmissions; }
    public void setPendingSubmissions(List<HomeworkReviewItem> pendingSubmissions) { this.pendingSubmissions = pendingSubmissions; }
    public List<HomeworkReviewItem> getReviewedSubmissions() { return reviewedSubmissions; }
    public void setReviewedSubmissions(List<HomeworkReviewItem> reviewedSubmissions) { this.reviewedSubmissions = reviewedSubmissions; }
    public Map<Integer, String> getAvailableClasses() { return availableClasses; }
    public void setAvailableClasses(Map<Integer, String> availableClasses) { this.availableClasses = availableClasses; }
    public Map<Integer, String> getAvailableSubjects() { return availableSubjects; }
    public void setAvailableSubjects(Map<Integer, String> availableSubjects) { this.availableSubjects = availableSubjects; }
    public Integer getSelectedClassId() { return selectedClassId; }
    public void setSelectedClassId(Integer selectedClassId) { this.selectedClassId = selectedClassId; }
    public Integer getSelectedSubjectId() { return selectedSubjectId; }
    public void setSelectedSubjectId(Integer selectedSubjectId) { this.selectedSubjectId = selectedSubjectId; }
    public String getSelectedClassName() { return selectedClassName; }
    public void setSelectedClassName(String selectedClassName) { this.selectedClassName = selectedClassName; }
    public String getSelectedSubjectName() { return selectedSubjectName; }
    public void setSelectedSubjectName(String selectedSubjectName) { this.selectedSubjectName = selectedSubjectName; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getActiveTab() { return activeTab; }
    public void setActiveTab(String activeTab) { this.activeTab = activeTab; }

    // Новые геттеры и сеттеры
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }
    public String getDateFrom() { return dateFrom; }
    public void setDateFrom(String dateFrom) { this.dateFrom = dateFrom; }
    public String getDateTo() { return dateTo; }
    public void setDateTo(String dateTo) { this.dateTo = dateTo; }

    // Вспомогательные методы для пагинации
    public boolean hasPrevious() { return currentPage > 0; }
    public boolean hasNext() { return currentPage < totalPages - 1; }
    public int getPreviousPage() { return Math.max(currentPage - 1, 0); }
    public int getNextPage() { return Math.min(currentPage + 1, totalPages - 1); }

    // Вспомогательные методы для количества
    public int getPendingCount() {
        return pendingSubmissions != null ? pendingSubmissions.size() : 0;
    }
    public int getReviewedCount() {
        return reviewedSubmissions != null ? reviewedSubmissions.size() : 0;
    }
}