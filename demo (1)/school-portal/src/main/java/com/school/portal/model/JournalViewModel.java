package com.school.portal.model;

import java.time.LocalDate;
import java.util.List;

// ViewModel для страницы журнала
public class JournalViewModel {
    private int selectedClassId;
    private int selectedSubjectId;
    private LocalDate weekStart;
    private java.util.Map<Integer, String> classes;
    private java.util.Map<Integer, String> subjects;
    private java.util.Map<String, String> weeks;
    private List<Schedule> lessonsForWeek;
    private List<JournalRow> rows;

    public JournalViewModel() {}

    public int getSelectedClassId() { return selectedClassId; }
    public void setSelectedClassId(int selectedClassId) { this.selectedClassId = selectedClassId; }

    public int getSelectedSubjectId() { return selectedSubjectId; }
    public void setSelectedSubjectId(int selectedSubjectId) { this.selectedSubjectId = selectedSubjectId; }

    public LocalDate getWeekStart() { return weekStart; }
    public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }

    public java.util.Map<Integer, String> getClasses() { return classes; }
    public void setClasses(java.util.Map<Integer, String> classes) { this.classes = classes; }

    public java.util.Map<Integer, String> getSubjects() { return subjects; }
    public void setSubjects(java.util.Map<Integer, String> subjects) { this.subjects = subjects; }

    public java.util.Map<String, String> getWeeks() { return weeks; }
    public void setWeeks(java.util.Map<String, String> weeks) { this.weeks = weeks; }

    public List<Schedule> getLessonsForWeek() { return lessonsForWeek; }
    public void setLessonsForWeek(List<Schedule> lessonsForWeek) { this.lessonsForWeek = lessonsForWeek; }

    public List<JournalRow> getRows() { return rows; }
    public void setRows(List<JournalRow> rows) { this.rows = rows; }
}