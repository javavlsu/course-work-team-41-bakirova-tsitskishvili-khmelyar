package com.school.portal.model;

import java.util.List;
import java.util.Map;

public class TeacherClassGradesViewModel {
    private List<TeacherStudentGradeItem> students;
    private Map<Integer, String> availableClasses;
    private Map<Integer, String> availableSubjects;
    private List<String> availableQuarters;
    private Integer selectedClassId;
    private Integer selectedSubjectId;
    private String selectedQuarter;
    private String selectedClassName;
    private String selectedSubjectName;

    // Геттеры и сеттеры
    public List<TeacherStudentGradeItem> getStudents() { return students; }
    public void setStudents(List<TeacherStudentGradeItem> students) { this.students = students; }

    public Map<Integer, String> getAvailableClasses() { return availableClasses; }
    public void setAvailableClasses(Map<Integer, String> availableClasses) { this.availableClasses = availableClasses; }

    public Map<Integer, String> getAvailableSubjects() { return availableSubjects; }
    public void setAvailableSubjects(Map<Integer, String> availableSubjects) { this.availableSubjects = availableSubjects; }

    public List<String> getAvailableQuarters() { return availableQuarters; }
    public void setAvailableQuarters(List<String> availableQuarters) { this.availableQuarters = availableQuarters; }

    public Integer getSelectedClassId() { return selectedClassId; }
    public void setSelectedClassId(Integer selectedClassId) { this.selectedClassId = selectedClassId; }

    public Integer getSelectedSubjectId() { return selectedSubjectId; }
    public void setSelectedSubjectId(Integer selectedSubjectId) { this.selectedSubjectId = selectedSubjectId; }

    public String getSelectedQuarter() { return selectedQuarter; }
    public void setSelectedQuarter(String selectedQuarter) { this.selectedQuarter = selectedQuarter; }

    public String getSelectedClassName() { return selectedClassName; }
    public void setSelectedClassName(String selectedClassName) { this.selectedClassName = selectedClassName; }

    public String getSelectedSubjectName() { return selectedSubjectName; }
    public void setSelectedSubjectName(String selectedSubjectName) { this.selectedSubjectName = selectedSubjectName; }
}