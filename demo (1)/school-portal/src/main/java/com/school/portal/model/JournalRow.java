package com.school.portal.model;

// Модель для строки журнала
public class JournalRow {
    private Student student;
    private java.util.Map<Integer, CellData> cells;

    public JournalRow() {
        this.cells = new java.util.HashMap<>();
    }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public java.util.Map<Integer, CellData> getCells() { return cells; }
    public void setCells(java.util.Map<Integer, CellData> cells) { this.cells = cells; }
}