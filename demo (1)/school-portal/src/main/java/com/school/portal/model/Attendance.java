package com.school.portal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentId", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LessonId", nullable = false)
    private Schedule lesson;

    @Column(name = "Status", length = 1, nullable = false)
    private String status;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Schedule getLesson() { return lesson; }
    public void setLesson(Schedule lesson) { this.lesson = lesson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}