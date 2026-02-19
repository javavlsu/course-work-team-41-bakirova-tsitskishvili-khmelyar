package com.school.portal.model;

import java.util.List;

public class StudentGradesViewModel {
    private String studentFullName;
    private String className;
    private List<StudentSubjectItem> subjects;
    private String selectedQuarter;
    private List<String> availableQuarters;
    private boolean isParentView;

    // Геттеры и сеттеры
    public String getStudentFullName() { return studentFullName; }
    public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public List<StudentSubjectItem> getSubjects() { return subjects; }
    public void setSubjects(List<StudentSubjectItem> subjects) { this.subjects = subjects; }

    public String getSelectedQuarter() { return selectedQuarter; }
    public void setSelectedQuarter(String selectedQuarter) { this.selectedQuarter = selectedQuarter; }

    public List<String> getAvailableQuarters() { return availableQuarters; }
    public void setAvailableQuarters(List<String> availableQuarters) { this.availableQuarters = availableQuarters; }

    public boolean isParentView() { return isParentView; }
    public void setParentView(boolean parentView) { isParentView = parentView; }
}