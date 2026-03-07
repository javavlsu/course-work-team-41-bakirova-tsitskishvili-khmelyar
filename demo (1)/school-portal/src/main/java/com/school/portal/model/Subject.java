package com.school.portal.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SubjectId")
    private Integer subjectId;

    @Column(name = "SubjectName", nullable = false, unique = true, length = 100)
    private String subjectName;

    @OneToMany(mappedBy = "subject")
    private Set<ClassSubjectTeacher> classSubjectTeachers;

    @OneToMany(mappedBy = "subject")
    private Set<ScheduleTemplate> scheduleTemplates;

    @OneToMany(mappedBy = "subject")
    private Set<Schedule> schedules;

    public Subject() {}

    public Subject(String subjectName) {
        this.subjectName = subjectName;
    }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Set<ClassSubjectTeacher> getClassSubjectTeachers() { return classSubjectTeachers; }
    public void setClassSubjectTeachers(Set<ClassSubjectTeacher> classSubjectTeachers) { this.classSubjectTeachers = classSubjectTeachers; }

    public Set<ScheduleTemplate> getScheduleTemplates() { return scheduleTemplates; }
    public void setScheduleTemplates(Set<ScheduleTemplate> scheduleTemplates) { this.scheduleTemplates = scheduleTemplates; }

    public Set<Schedule> getSchedules() { return schedules; }
    public void setSchedules(Set<Schedule> schedules) { this.schedules = schedules; }
}