package com.school.portal.model;

import java.util.List;

public class StudentSubjectItem {
    private String subjectName;
    private double averageGrade;
    private int quarterFinalGrade;
    private int totalAbsences;
    private int absentTypeH;
    private int absentTypeU;
    private int absentTypeB;
    private int totalLessonsInPeriod;
    private List<Integer> allGrades;

    // Геттеры и сеттеры
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public double getAverageGrade() { return averageGrade; }
    public void setAverageGrade(double averageGrade) { this.averageGrade = averageGrade; }

    public int getQuarterFinalGrade() { return quarterFinalGrade; }
    public void setQuarterFinalGrade(int quarterFinalGrade) { this.quarterFinalGrade = quarterFinalGrade; }

    public int getTotalAbsences() { return totalAbsences; }
    public void setTotalAbsences(int totalAbsences) { this.totalAbsences = totalAbsences; }

    public int getAbsentTypeH() { return absentTypeH; }
    public void setAbsentTypeH(int absentTypeH) { this.absentTypeH = absentTypeH; }

    public int getAbsentTypeU() { return absentTypeU; }
    public void setAbsentTypeU(int absentTypeU) { this.absentTypeU = absentTypeU; }

    public int getAbsentTypeB() { return absentTypeB; }
    public void setAbsentTypeB(int absentTypeB) { this.absentTypeB = absentTypeB; }

    public int getTotalLessonsInPeriod() { return totalLessonsInPeriod; }
    public void setTotalLessonsInPeriod(int totalLessonsInPeriod) { this.totalLessonsInPeriod = totalLessonsInPeriod; }

    public List<Integer> getAllGrades() { return allGrades; }
    public void setAllGrades(List<Integer> allGrades) { this.allGrades = allGrades; }

    // Вспомогательные методы для Thymeleaf
    public int getGradeCount() {
        return allGrades != null ? allGrades.size() : 0;
    }

    public String getAllGradesString() {
        if (allGrades == null || allGrades.isEmpty()) return "";
        return allGrades.toString().replace("[", "").replace("]", "");
    }

    public int getGradeCount5() { return countGrades(5); }
    public int getGradeCount4() { return countGrades(4); }
    public int getGradeCount3() { return countGrades(3); }
    public int getGradeCount2() { return countGrades(2); }
    public int getGradeCount1() { return countGrades(1); }
    public int getGradeCount0() { return countGrades(0); }

    private int countGrades(int grade) {
        if (allGrades == null) return 0;
        return (int) allGrades.stream().filter(g -> g == grade).count();
    }

    public double getPercent5() { return getGradeCount() > 0 ? (getGradeCount5() * 100.0 / getGradeCount()) : 0; }
    public double getPercent4() { return getGradeCount() > 0 ? (getGradeCount4() * 100.0 / getGradeCount()) : 0; }
    public double getPercent3() { return getGradeCount() > 0 ? (getGradeCount3() * 100.0 / getGradeCount()) : 0; }
    public double getPercent2() { return getGradeCount() > 0 ? (getGradeCount2() * 100.0 / getGradeCount()) : 0; }
    public double getPercent1() { return getGradeCount() > 0 ? (getGradeCount1() * 100.0 / getGradeCount()) : 0; }
    public double getPercent0() { return getGradeCount() > 0 ? (getGradeCount0() * 100.0 / getGradeCount()) : 0; }
}