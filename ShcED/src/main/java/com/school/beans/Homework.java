package main.java.com.school.beans;

import java.sql.Date;

public class Homework {
    private int homeworkId;
    private int lessonId;
    private Date date;
    private String text;
    private byte[] filePath;
    private int studentId;

    public Homework() {}

    public Homework(int homeworkId, int lessonId, Date date, String text, byte[] filePath, int studentId) {
        this.homeworkId = homeworkId;
        this.lessonId = lessonId;
        this.date = date;
        this.text = text;
        this.filePath = filePath;
        this.studentId = studentId;
    }

    public Homework(int lessonId, Date date, int studentId) {
        this(0, lessonId, date, null, null, studentId);
    }

    public int getHomeworkId() { return homeworkId; }
    public void setHomeworkId(int homeworkId) { this.homeworkId = homeworkId; }

    public int getLessonId() { return lessonId; }
    public void setLessonId(int lessonId) { this.lessonId = lessonId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public byte[] getFilePath() { return filePath; }
    public void setFilePath(byte[] filePath) { this.filePath = filePath; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    @Override
    public String toString() {
        return "Homework{homeworkId=" + homeworkId + ", lessonId=" + lessonId + ", studentId=" + studentId + "}";
    }
}