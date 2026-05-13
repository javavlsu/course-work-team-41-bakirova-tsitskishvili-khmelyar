package main.java.com.school.beans;

public class SchoolClass {
    private int classId;
    private int classNumber;
    private String classLetter;
    private int classTeacherId;

    public SchoolClass() {}

    public SchoolClass(int classId, int classNumber, String classLetter, int classTeacherId) {
        this.classId = classId;
        this.classNumber = classNumber;
        this.classLetter = classLetter;
        this.classTeacherId = classTeacherId;
    }

    public SchoolClass(int classNumber, String classLetter, int classTeacherId) {
        this(0, classNumber, classLetter, classTeacherId);
    }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public int getClassNumber() { return classNumber; }
    public void setClassNumber(int classNumber) { this.classNumber = classNumber; }

    public String getClassLetter() { return classLetter; }
    public void setClassLetter(String classLetter) { this.classLetter = classLetter; }

    public int getClassTeacherId() { return classTeacherId; }
    public void setClassTeacherId(int classTeacherId) { this.classTeacherId = classTeacherId; }

    @Override
    public String toString() {
        return "SchoolClass{classId=" + classId + ", class=" + classNumber + classLetter + "}";
    }
}