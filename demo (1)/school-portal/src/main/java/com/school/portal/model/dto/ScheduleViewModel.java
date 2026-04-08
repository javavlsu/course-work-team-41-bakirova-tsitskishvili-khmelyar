package com.school.portal.model.dto;

import com.school.portal.model.SchoolClass;
import com.school.portal.model.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ScheduleViewModel {
    private Map<DayOfWeek, List<ScheduleItemViewModel>> scheduleByDay;
    private LocalDate selectedDate;
    private LocalDate startOfWeek;
    private boolean isPersonalView;
    private boolean isAdminView;
    private Integer selectedClassId;
    private String filterType;
    private Integer selectedTeacherId;
    private String selectedClassName;

    private List<com.school.portal.model.User> availableTeachers;
    private List<com.school.portal.model.SchoolClass> availableClasses;

    public Map<DayOfWeek, List<ScheduleItemViewModel>> getScheduleByDay() {
        return scheduleByDay;
    }

    public void setScheduleByDay(Map<DayOfWeek, List<ScheduleItemViewModel>> scheduleByDay) {
        this.scheduleByDay = scheduleByDay;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
    }

    public LocalDate getStartOfWeek() {
        return startOfWeek;
    }

    public void setStartOfWeek(LocalDate startOfWeek) {
        this.startOfWeek = startOfWeek;
    }

    public boolean isPersonalView() {
        return isPersonalView;
    }

    public void setPersonalView(boolean personalView) {
        isPersonalView = personalView;
    }

    public boolean isAdminView() {
        return isAdminView;
    }

    public void setAdminView(boolean adminView) {
        isAdminView = adminView;
    }

    public Integer getSelectedClassId() {
        return selectedClassId;
    }

    public void setSelectedClassId(Integer selectedClassId) {
        this.selectedClassId = selectedClassId;
    }

    public String getFilterType() { return filterType; }

    public void setFilterType(String filterType) { this.filterType = filterType; }

    public Integer getSelectedTeacherId() { return selectedTeacherId; }

    public void setSelectedTeacherId(Integer selectedTeacherId) { this.selectedTeacherId = selectedTeacherId; }

    public String getSelectedClassName() { return selectedClassName; }

    public void setSelectedClassName(String selectedClassName) { this.selectedClassName = selectedClassName; }

    public List<User> getAvailableTeachers() { return availableTeachers; }

    public void setAvailableTeachers(List<User> availableTeachers) { this.availableTeachers = availableTeachers; }

    public List<SchoolClass> getAvailableClasses() { return availableClasses; }

    public void setAvailableClasses(List<SchoolClass> availableClasses) { this.availableClasses = availableClasses; }
}