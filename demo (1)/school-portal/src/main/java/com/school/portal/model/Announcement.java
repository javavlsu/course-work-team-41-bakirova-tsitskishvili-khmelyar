package com.school.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcement")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AnnouncementId")
    private Integer announcementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ClassId")
    private SchoolClass schoolClass;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "Text", columnDefinition = "TEXT", nullable = false)
    private String text;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Integer getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(Integer announcementId) { this.announcementId = announcementId; }

    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getFormattedDate() {
        if (createdAt != null) {
            return String.format("%02d.%02d.%04d %02d:%02d",
                    createdAt.getDayOfMonth(),
                    createdAt.getMonthValue(),
                    createdAt.getYear(),
                    createdAt.getHour(),
                    createdAt.getMinute());
        }
        return "";
    }
}