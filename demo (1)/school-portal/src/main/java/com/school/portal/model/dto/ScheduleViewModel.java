package com.school.portal.model.dto;

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
}