package com.school.portal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ScheduleTemplate")
public class ScheduleTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ClassId", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SubjectId", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeacherId", nullable = false)
    private User teacher;

    @Column(name = "DayOfWeek", nullable = false)
    private Byte dayOfWeek;

    @Column(name = "LessonNumber", nullable = false)
    private Integer lessonNumber;

    @Column(name = "Room", length = 10)
    private String room;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }

    public Byte getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Byte dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Integer getLessonNumber() { return lessonNumber; }
    public void setLessonNumber(Integer lessonNumber) { this.lessonNumber = lessonNumber; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
}