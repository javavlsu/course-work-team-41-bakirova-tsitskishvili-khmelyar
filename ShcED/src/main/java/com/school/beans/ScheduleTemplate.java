package main.java.com.school.beans;

public class ScheduleTemplate {
    private int id;
    private int classId;
    private int subjectId;
    private int teacherId;
    private int dayOfWeek;
    private int lessonNumber;
    private String room;

    public ScheduleTemplate() {}

    public ScheduleTemplate(int id, int classId, int subjectId, int teacherId, int dayOfWeek, int lessonNumber, String room) {
        this.id = id;
        this.classId = classId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.dayOfWeek = dayOfWeek;
        this.lessonNumber = lessonNumber;
        this.room = room;
    }

    public ScheduleTemplate(int classId, int subjectId, int teacherId, int dayOfWeek, int lessonNumber, String room) {
        this(0, classId, subjectId, teacherId, dayOfWeek, lessonNumber, room);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public int getLessonNumber() { return lessonNumber; }
    public void setLessonNumber(int lessonNumber) { this.lessonNumber = lessonNumber; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    @Override
    public String toString() {
        return "ScheduleTemplate{id=" + id + ", classId=" + classId + ", day=" + dayOfWeek + ", lesson=" + lessonNumber + "}";
    }
}