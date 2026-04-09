package com.school.portal.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "schoolClass")
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClassId")
    private Integer classId;

    @Column(name = "ClassNumber", nullable = false)
    private Integer classNumber;

    @Column(name = "ClassLetter", nullable = false, length = 1)
    private String classLetter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ClassTeacherId")
    private User classTeacher;

    @OneToMany(mappedBy = "schoolClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Announcement> announcements;

    @OneToMany(mappedBy = "schoolClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentClass> studentClasses;

    @OneToMany(mappedBy = "schoolClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClassSubjectTeacher> classSubjectTeachers;

    @OneToMany(mappedBy = "schoolClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Schedule> schedules;

    @OneToMany(mappedBy = "schoolClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ScheduleTemplate> scheduleTemplates;

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }

    public Integer getClassNumber() { return classNumber; }
    public void setClassNumber(Integer classNumber) { this.classNumber = classNumber; }

    public String getClassLetter() { return classLetter; }
    public void setClassLetter(String classLetter) { this.classLetter = classLetter; }

    public User getClassTeacher() { return classTeacher; }
    public void setClassTeacher(User classTeacher) { this.classTeacher = classTeacher; }

    public String getClassName() {
        return classNumber + " \"" + classLetter + "\"";
    }

    public Set<Announcement> getAnnouncements() { return announcements; }
    public void setAnnouncements(Set<Announcement> announcements) { this.announcements = announcements; }

    public Set<StudentClass> getStudentClasses() { return studentClasses; }
    public void setStudentClasses(Set<StudentClass> studentClasses) { this.studentClasses = studentClasses; }

    public Set<ClassSubjectTeacher> getClassSubjectTeachers() { return classSubjectTeachers; }
    public void setClassSubjectTeachers(Set<ClassSubjectTeacher> classSubjectTeachers) { this.classSubjectTeachers = classSubjectTeachers; }

    public Set<Schedule> getSchedules() { return schedules; }
    public void setSchedules(Set<Schedule> schedules) { this.schedules = schedules; }

    public Set<ScheduleTemplate> getScheduleTemplates() { return scheduleTemplates; }
    public void setScheduleTemplates(Set<ScheduleTemplate> scheduleTemplates) { this.scheduleTemplates = scheduleTemplates; }
}