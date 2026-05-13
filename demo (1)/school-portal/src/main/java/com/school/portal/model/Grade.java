package com.school.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grade")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GradeId")
    private Integer gradeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LessonId", nullable = false)
    private Schedule lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HomeworkId")
    private Homework homework;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentId", nullable = false)
    private User student;

    @Column(name = "GradeValue")
    private Integer gradeValue;

    @Column(name = "Comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "Date", nullable = false)
    private LocalDateTime date;

    @OneToOne(mappedBy = "grade")
    private TransactionHistory transaction;

    @PrePersist
    protected void onCreate() {
        date = LocalDateTime.now();
    }

    public Integer getGradeId() { return gradeId; }
    public void setGradeId(Integer gradeId) { this.gradeId = gradeId; }

    public Schedule getLesson() { return lesson; }
    public void setLesson(Schedule lesson) { this.lesson = lesson; }

    public Homework getHomework() { return homework; }
    public void setHomework(Homework homework) { this.homework = homework; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Integer getGradeValue() { return gradeValue; }
    public void setGradeValue(Integer gradeValue) { this.gradeValue = gradeValue; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public TransactionHistory getTransaction() { return transaction; }
    public void setTransaction(TransactionHistory transaction) { this.transaction = transaction; }
}