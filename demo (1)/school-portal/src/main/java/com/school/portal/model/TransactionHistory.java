package com.school.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TransactionHistory")
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TransactionId")
    private Integer transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentId", nullable = false)
    private User student;

    @Column(name = "Amount", nullable = false)
    private Integer amount;

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "Date", nullable = false)
    private LocalDateTime date;

    @OneToOne
    @JoinColumn(name = "MerchRequestId")
    private MerchRequest merchRequest;

    @OneToOne
    @JoinColumn(name = "LessonId")
    private Schedule lesson;

    @OneToOne
    @JoinColumn(name = "GradeId")
    private Grade grade;

    @PrePersist
    protected void onCreate() {
        date = LocalDateTime.now();
    }

    public Integer getTransactionId() { return transactionId; }
    public void setTransactionId(Integer transactionId) { this.transactionId = transactionId; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public MerchRequest getMerchRequest() { return merchRequest; }
    public void setMerchRequest(MerchRequest merchRequest) { this.merchRequest = merchRequest; }

    public Schedule getLesson() { return lesson; }
    public void setLesson(Schedule lesson) { this.lesson = lesson; }

    public Grade getGrade() { return grade; }
    public void setGrade(Grade grade) { this.grade = grade; }
}