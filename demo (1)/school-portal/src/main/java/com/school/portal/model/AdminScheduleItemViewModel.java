package com.school.portal.model;

import java.time.DayOfWeek;

public class AdminScheduleItemViewModel {
    private Integer scheduleId;
    private DayOfWeek dayOfWeek;
    private Integer lessonNumber;
    private String lessonTime;
    private Integer classId;
    private String className;
    private Integer subjectId;
    private String subjectName;
    private Integer teacherId;
    private String teacherFullName;
    private String classroom;
    private Boolean isCustomLesson;

    // Геттеры и сеттеры
    public Integer getScheduleId() { return scheduleId; }
    public void setScheduleId(Integer scheduleId) { this.scheduleId = scheduleId; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Integer getLessonNumber() { return lessonNumber; }
    public void setLessonNumber(Integer lessonNumber) { this.lessonNumber = lessonNumber; }

    public String getLessonTime() { return lessonTime; }
    public void setLessonTime(String lessonTime) { this.lessonTime = lessonTime; }

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }

    public String getTeacherFullName() { return teacherFullName; }
    public void setTeacherFullName(String teacherFullName) { this.teacherFullName = teacherFullName; }

    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }

    public Boolean getIsCustomLesson() { return isCustomLesson; }
    public void setIsCustomLesson(Boolean customLesson) { isCustomLesson = customLesson; }
}