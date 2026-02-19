package com.school.portal.model;

import java.time.LocalDateTime;

public class Announcement {
    private Long id;
    private String title;
    private String text;
    private LocalDateTime createdAt;
    private String formattedDate;

    // Конструкторы
    public Announcement() {}

    public Announcement(Long id, String title, String text, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.createdAt = createdAt;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFormattedDate() { return formattedDate; }
    public void setFormattedDate(String formattedDate) { this.formattedDate = formattedDate; }
}