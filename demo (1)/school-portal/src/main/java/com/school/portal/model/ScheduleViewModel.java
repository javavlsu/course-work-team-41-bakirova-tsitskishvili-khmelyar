package com.school.portal.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ScheduleViewModel {
    private Map<DayOfWeek, List<ScheduleItemViewModel>> scheduleByDay;
    private java.time.LocalDate selectedDate;
    private java.time.LocalDate startOfWeek;
    private boolean isPersonalView;
    private boolean isAdminView;
    private Integer selectedClassId;

    // Геттеры и сеттеры
    public Map<DayOfWeek, List<ScheduleItemViewModel>> getScheduleByDay() { return scheduleByDay; }
    public void setScheduleByDay(Map<DayOfWeek, List<ScheduleItemViewModel>> scheduleByDay) { this.scheduleByDay = scheduleByDay; }

    public java.time.LocalDate getSelectedDate() { return selectedDate; }
    public void setSelectedDate(java.time.LocalDate selectedDate) { this.selectedDate = selectedDate; }

    public java.time.LocalDate getStartOfWeek() { return startOfWeek; }
    public void setStartOfWeek(java.time.LocalDate startOfWeek) { this.startOfWeek = startOfWeek; }

    public boolean isPersonalView() { return isPersonalView; }
    public void setPersonalView(boolean personalView) { isPersonalView = personalView; }

    public boolean isAdminView() { return isAdminView; }
    public void setAdminView(boolean adminView) { isAdminView = adminView; }

    public Integer getSelectedClassId() { return selectedClassId; }
    public void setSelectedClassId(Integer selectedClassId) { this.selectedClassId = selectedClassId; }
}