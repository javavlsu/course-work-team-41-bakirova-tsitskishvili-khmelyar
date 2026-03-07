package com.school.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LessonId")
    private Integer lessonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ClassId", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SubjectId", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeacherId", nullable = false)
    private User teacher;

    @Column(name = "Room", length = 10)
    private String room;

    @Column(name = "LessonTopic", columnDefinition = "TEXT")
    private String lessonTopic;

    @Column(name = "HomeworkText", columnDefinition = "TEXT")
    private String homeworkText;

    @Column(name = "LessonDateTime", nullable = false)
    private LocalDateTime lessonDateTime;

    @OneToMany(mappedBy = "lesson")
    private Set<Homework> homeworks;

    @OneToMany(mappedBy = "lesson")
    private Set<Grade> grades;

    @OneToMany(mappedBy = "lesson")
    private Set<Remark> remarks;

    @OneToMany(mappedBy = "lesson")
    private Set<Attendance> attendances;

    public Integer getLessonId() { return lessonId; }
    public void setLessonId(Integer lessonId) { this.lessonId = lessonId; }

    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getLessonTopic() { return lessonTopic; }
    public void setLessonTopic(String lessonTopic) { this.lessonTopic = lessonTopic; }

    public String getHomeworkText() { return homeworkText; }
    public void setHomeworkText(String homeworkText) { this.homeworkText = homeworkText; }

    public LocalDateTime getLessonDateTime() { return lessonDateTime; }
    public void setLessonDateTime(LocalDateTime lessonDateTime) { this.lessonDateTime = lessonDateTime; }

    public String getFormattedDate() {
        if (lessonDateTime != null) {
            return String.format("%02d.%02d",
                    lessonDateTime.getDayOfMonth(),
                    lessonDateTime.getMonthValue());
        }
        return "";
    }

    public Set<Homework> getHomeworks() { return homeworks; }
    public void setHomeworks(Set<Homework> homeworks) { this.homeworks = homeworks; }

    public Set<Grade> getGrades() { return grades; }
    public void setGrades(Set<Grade> grades) { this.grades = grades; }

    public Set<Remark> getRemarks() { return remarks; }
    public void setRemarks(Set<Remark> remarks) { this.remarks = remarks; }

    public Set<Attendance> getAttendances() { return attendances; }
    public void setAttendances(Set<Attendance> attendances) { this.attendances = attendances; }
}