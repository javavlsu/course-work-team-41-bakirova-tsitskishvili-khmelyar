package main.java.com.school.beans;

public class StudentClass {
    private int id;
    private int studentId;
    private int classId;

    public StudentClass() {}

    public StudentClass(int id, int studentId, int classId) {
        this.id = id;
        this.studentId = studentId;
        this.classId = classId;
    }

    public StudentClass(int studentId, int classId) {
        this(0, studentId, classId);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    @Override
    public String toString() {
        return "StudentClass{id=" + id + ", studentId=" + studentId + ", classId=" + classId + "}";
    }
}