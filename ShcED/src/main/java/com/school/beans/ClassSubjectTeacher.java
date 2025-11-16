package main.java.com.school.beans;

public class ClassSubjectTeacher {
    private int id;
    private int classId;
    private int subjectId;
    private int teacherId;

    public ClassSubjectTeacher() {}

    public ClassSubjectTeacher(int id, int classId, int subjectId, int teacherId) {
        this.id = id;
        this.classId = classId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
    }

    public ClassSubjectTeacher(int classId, int subjectId, int teacherId) {
        this(0, classId, subjectId, teacherId);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    @Override
    public String toString() {
        return "ClassSubjectTeacher{id=" + id + ", classId=" + classId + ", subjectId=" + subjectId + ", teacherId=" + teacherId + "}";
    }
}