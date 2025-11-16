package main.java.com.school.beans;

public class Attendance {
    private int id;
    private int studentId;
    private int lessonId;
    private String status;

    public Attendance() {}

    public Attendance(int id, int studentId, int lessonId, String status) {
        this.id = id;
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.status = status;
    }

    public Attendance(int studentId, int lessonId, String status) {
        this(0, studentId, lessonId, status);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getLessonId() { return lessonId; }
    public void setLessonId(int lessonId) { this.lessonId = lessonId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Attendance{id=" + id + ", studentId=" + studentId + ", status='" + status + "'}";
    }
}