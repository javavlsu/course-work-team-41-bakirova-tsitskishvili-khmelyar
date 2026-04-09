package com.school.portal.model.dto;

public class CellData {
    private String value;
    private boolean isAttendance;
    private boolean hasComment;
    private String comment;
    private boolean hasRemark;
    private String remark;

    public CellData() {}

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public boolean isAttendance() { return isAttendance; }
    public void setAttendance(boolean attendance) { isAttendance = attendance; }

    public boolean isHasComment() { return hasComment; }
    public void setHasComment(boolean hasComment) { this.hasComment = hasComment; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public boolean isHasRemark() { return hasRemark; }
    public void setHasRemark(boolean hasRemark) { this.hasRemark = hasRemark; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}