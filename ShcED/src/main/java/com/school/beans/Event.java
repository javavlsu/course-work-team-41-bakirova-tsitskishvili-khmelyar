package main.java.com.school.beans;

import java.sql.Date;

public class Event {
    private int eventId;
    private Date startDate;
    private Date endDate;
    private String title;
    private String text;
    private byte[] filePath;
    private Integer mainOrganizerId;

    public Event() {}

    public Event(int eventId, Date startDate, Date endDate, String title, String text, byte[] filePath, Integer mainOrganizerId) {
        this.eventId = eventId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.text = text;
        this.filePath = filePath;
        this.mainOrganizerId = mainOrganizerId;
    }

    public Event(Date startDate, String title, String text) {
        this(0, startDate, null, title, text, null, null);
    }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public byte[] getFilePath() { return filePath; }
    public void setFilePath(byte[] filePath) { this.filePath = filePath; }

    public Integer getMainOrganizerId() { return mainOrganizerId; }
    public void setMainOrganizerId(Integer mainOrganizerId) { this.mainOrganizerId = mainOrganizerId; }

    @Override
    public String toString() {
        return "Event{eventId=" + eventId + ", title='" + title + "', startDate=" + startDate + "}";
    }
}