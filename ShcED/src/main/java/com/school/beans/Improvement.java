package main.java.com.school.beans;

import java.sql.Date;

public class Improvement {
    private int id;
    private Date createdAt;
    private int fromUserId;
    private String title;
    private String text;

    public Improvement() {}

    public Improvement(int id, Date createdAt, int fromUserId, String title, String text) {
        this.id = id;
        this.createdAt = createdAt;
        this.fromUserId = fromUserId;
        this.title = title;
        this.text = text;
    }

    public Improvement(Date createdAt, int fromUserId, String text) {
        this(0, createdAt, fromUserId, null, text);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public int getFromUserId() { return fromUserId; }
    public void setFromUserId(int fromUserId) { this.fromUserId = fromUserId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    @Override
    public String toString() {
        return "Improvement{id=" + id + ", fromUserId=" + fromUserId + ", title='" + title + "'}";
    }
}