package main.java.com.school.beans;

import java.sql.Date;

public class Announcement {
    private int announcementId;
    private int classId;
    private Date createdAt;
    private String title;
    private String text;

    public Announcement() {}

    public Announcement(int announcementId, int classId, Date createdAt, String title, String text) {
        this.announcementId = announcementId;
        this.classId = classId;
        this.createdAt = createdAt;
        this.title = title;
        this.text = text;
    }

    public Announcement(int classId, Date createdAt, String title, String text) {
        this(0, classId, createdAt, title, text);
    }

    public int getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(int announcementId) { this.announcementId = announcementId; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    @Override
    public String toString() {
        return "Announcement{announcementId=" + announcementId + ", title='" + title + "'}";
    }
}