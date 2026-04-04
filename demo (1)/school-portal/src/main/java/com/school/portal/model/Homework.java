package com.school.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "homework")
public class Homework {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HomeworkId")
    private Integer homeworkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LessonId", nullable = false)
    private Schedule lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentId", nullable = false)
    private User student;

    @Column(name = "Date", nullable = false)
    private LocalDateTime date;

    @Column(name = "Text", columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "Status", nullable = false)
    private Integer status;

    @Column(name = "FilePath")
    private byte[] filePath;

    @Column(name = "TeacherComment", columnDefinition = "TEXT")
    private String teacherComment;

    @OneToMany(mappedBy = "homework")
    private Set<Grade> grades;

    @PrePersist
    protected void onCreate() {
        date = LocalDateTime.now();
        if (status == null) status = 0;
    }

    public Integer getHomeworkId() { return homeworkId; }
    public void setHomeworkId(Integer homeworkId) { this.homeworkId = homeworkId; }

    public Schedule getLesson() { return lesson; }
    public void setLesson(Schedule lesson) { this.lesson = lesson; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public byte[] getFilePath() { return filePath; }
    public void setFilePath(byte[] filePath) { this.filePath = filePath; }

    public String getTeacherComment() { return teacherComment; }
    public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }

    public Set<Grade> getGrades() { return grades; }
    public void setGrades(Set<Grade> grades) { this.grades = grades; }

    public String getReviewStatus() {
        switch (status) {
            case 0: return "Не сдано";
            case 1: return "Сдано";
            case 2: return "Проверено";
            default: return "Неизвестно";
        }
    }
}