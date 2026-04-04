package com.school.portal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "remark")
public class Remark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LessonId", nullable = false)
    private Schedule lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentId", nullable = false)
    private User student;

    @Column(name = "Text", columnDefinition = "TEXT", nullable = false)
    private String text;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Schedule getLesson() { return lesson; }
    public void setLesson(Schedule lesson) { this.lesson = lesson; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}