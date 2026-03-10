package com.school.portal.model.dto;

import com.school.portal.model.Schedule;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class JournalViewModel {
    private int selectedClassId;
    private int selectedSubjectId;
    private LocalDate weekStart;
    private Map<Integer, String> classes;
    private Map<Integer, String> subjects;
    private Map<String, String> weeks;
    private List<Schedule> lessonsForWeek;
    private List<JournalRow> rows;

    public JournalViewModel() {}

    public int getSelectedClassId() {
        return selectedClassId;
    }

    public void setSelectedClassId(int selectedClassId) {
        this.selectedClassId = selectedClassId;
    }

    public int getSelectedSubjectId() {
        return selectedSubjectId;
    }

    public void setSelectedSubjectId(int selectedSubjectId) {
        this.selectedSubjectId = selectedSubjectId;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public Map<Integer, String> getClasses() {
        return classes;
    }

    public void setClasses(Map<Integer, String> classes) {
        this.classes = classes;
    }

    public Map<Integer, String> getSubjects() {
        return subjects;
    }

    public void setSubjects(Map<Integer, String> subjects) {
        this.subjects = subjects;
    }

    public Map<String, String> getWeeks() {
        return weeks;
    }

    public void setWeeks(Map<String, String> weeks) {
        this.weeks = weeks;
    }

    public List<Schedule> getLessonsForWeek() {
        return lessonsForWeek;
    }

    public void setLessonsForWeek(List<Schedule> lessonsForWeek) {
        this.lessonsForWeek = lessonsForWeek;
    }

    public List<JournalRow> getRows() {
        return rows;
    }

    public void setRows(List<JournalRow> rows) {
        this.rows = rows;
    }
}