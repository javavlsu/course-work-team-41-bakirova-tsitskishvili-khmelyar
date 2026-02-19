package com.school.portal.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AdminScheduleViewModel {
    private LocalDate startOfWeek;
    private String filterType;
    private Integer selectedTeacherId;
    private Integer selectedClassId;
    private List<User> availableTeachers;
    private List<SchoolClass> availableClasses;
    private Map<DayOfWeek, List<AdminScheduleItemViewModel>> scheduleByDay;
    private String selectedClassName; // Добавляем это поле
    private String selectedSubjectName; // Добавляем для полноты

    // Геттеры и сеттеры
    public LocalDate getStartOfWeek() { return startOfWeek; }
    public void setStartOfWeek(LocalDate startOfWeek) { this.startOfWeek = startOfWeek; }

    public String getFilterType() { return filterType; }
    public void setFilterType(String filterType) { this.filterType = filterType; }

    public Integer getSelectedTeacherId() { return selectedTeacherId; }
    public void setSelectedTeacherId(Integer selectedTeacherId) { this.selectedTeacherId = selectedTeacherId; }

    public Integer getSelectedClassId() { return selectedClassId; }
    public void setSelectedClassId(Integer selectedClassId) { this.selectedClassId = selectedClassId; }

    public List<User> getAvailableTeachers() { return availableTeachers; }
    public void setAvailableTeachers(List<User> availableTeachers) { this.availableTeachers = availableTeachers; }

    public List<SchoolClass> getAvailableClasses() { return availableClasses; }
    public void setAvailableClasses(List<SchoolClass> availableClasses) { this.availableClasses = availableClasses; }

    public Map<DayOfWeek, List<AdminScheduleItemViewModel>> getScheduleByDay() { return scheduleByDay; }
    public void setScheduleByDay(Map<DayOfWeek, List<AdminScheduleItemViewModel>> scheduleByDay) { this.scheduleByDay = scheduleByDay; }

    public String getSelectedClassName() { return selectedClassName; }
    public void setSelectedClassName(String selectedClassName) { this.selectedClassName = selectedClassName; }

    public String getSelectedSubjectName() { return selectedSubjectName; }
    public void setSelectedSubjectName(String selectedSubjectName) { this.selectedSubjectName = selectedSubjectName; }
}