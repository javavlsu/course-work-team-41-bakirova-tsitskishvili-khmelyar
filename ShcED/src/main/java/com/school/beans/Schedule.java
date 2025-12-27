package main.java.com.school.beans;

import java.sql.Date;

public class Schedule {
    private int lessonId;
    private Date date;
    private int lessonNumber;
    private int classId;
    private int subjectId;
    private int teacherId;
    private String room;
    private String lessonTopic;
    private String homeworkText;

    public Schedule() {}

    public Schedule(int lessonId, Date date, int lessonNumber, int classId, int subjectId,
                    int teacherId, String room, String lessonTopic, String homeworkText) {
        this.lessonId = lessonId;
        this.date = date;
        this.lessonNumber = lessonNumber;
        this.classId = classId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.room = room;
        this.lessonTopic = lessonTopic;
        this.homeworkText = homeworkText;
    }

    public Schedule(Date date, int lessonNumber, int classId, int subjectId, int teacherId, String room) {
        this(0, date, lessonNumber, classId, subjectId, teacherId, room, null, null);
    }

    public int getLessonId() { return lessonId; }
    public void setLessonId(int lessonId) { this.lessonId = lessonId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public int getLessonNumber() { return lessonNumber; }
    public void setLessonNumber(int lessonNumber) { this.lessonNumber = lessonNumber; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getLessonTopic() { return lessonTopic; }
    public void setLessonTopic(String lessonTopic) { this.lessonTopic = lessonTopic; }

    public String getHomeworkText() { return homeworkText; }
    public void setHomeworkText(String homeworkText) { this.homeworkText = homeworkText; }

    @Override
    public String toString() {
        return "Schedule{lessonId=" + lessonId + ", date=" + date + ", classId=" + classId + "}";
    }
}