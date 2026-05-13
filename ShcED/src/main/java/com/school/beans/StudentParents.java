package main.java.com.school.beans;

public class StudentParents {
    private int id;
    private int studentId;
    private int parentId;

    public StudentParents() {}

    public StudentParents(int id, int studentId, int parentId) {
        this.id = id;
        this.studentId = studentId;
        this.parentId = parentId;
    }

    public StudentParents(int studentId, int parentId) {
        this(0, studentId, parentId);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }

    @Override
    public String toString() {
        return "StudentParents{id=" + id + ", studentId=" + studentId + ", parentId=" + parentId + "}";
    }
}