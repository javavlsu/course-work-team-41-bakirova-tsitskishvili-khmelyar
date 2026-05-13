package com.school.portal.model.dto;

import com.school.portal.model.User;
import java.util.HashMap;
import java.util.Map;

public class JournalRow {
    private User student;
    private Map<Integer, CellData> cells = new HashMap<>();

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Map<Integer, CellData> getCells() { return cells; }
    public void setCells(Map<Integer, CellData> cells) { this.cells = cells; }
}