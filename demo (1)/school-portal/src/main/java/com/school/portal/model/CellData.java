package com.school.portal.model;

// Модель для данных ячейки
public class CellData {
    private String value;
    private boolean isAttendance;
    private boolean hasComment;
    private String comment;

    public CellData() {}

    public CellData(String value, boolean isAttendance) {
        this.value = value;
        this.isAttendance = isAttendance;
    }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public boolean isAttendance() { return isAttendance; }
    public void setAttendance(boolean attendance) { isAttendance = attendance; }

    public boolean isHasComment() { return hasComment; }
    public void setHasComment(boolean hasComment) { this.hasComment = hasComment; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}