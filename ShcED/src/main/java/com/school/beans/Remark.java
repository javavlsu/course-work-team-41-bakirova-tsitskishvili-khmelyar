package main.java.com.school.beans;

public class Remark {
    private int id;
    private int studentId;
    private int lessonId;
    private String text;

    public Remark() {}

    public Remark(int id, int studentId, int lessonId, String text) {
        this.id = id;
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.text = text;
    }

    public Remark(int studentId, int lessonId, String text) {
        this(0, studentId, lessonId, text);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getLessonId() { return lessonId; }
    public void setLessonId(int lessonId) { this.lessonId = lessonId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    @Override
    public String toString() {
        return "Remark{id=" + id + ", studentId=" + studentId + ", text='" + text + "'}";
    }
}