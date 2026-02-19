package com.school.portal.model;

public class SchoolClass {
    private int classId;
    private int classNumber;
    private String classLetter;
    private int classTeacherId; // ID классного руководителя

    // Конструктор без параметров
    public SchoolClass() {}

    // Конструктор с 4 параметрами (теперь включает classTeacherId)
    public SchoolClass(int classId, int classNumber, String classLetter, int classTeacherId) {
        this.classId = classId;
        this.classNumber = classNumber;
        this.classLetter = classLetter;
        this.classTeacherId = classTeacherId;
    }

    // Конструктор с 3 параметрами для обратной совместимости
    public SchoolClass(int classId, int classNumber, String classLetter) {
        this(classId, classNumber, classLetter, 0); // По умолчанию classTeacherId = 0
    }

    // Геттеры и сеттеры
    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public int getClassNumber() { return classNumber; }
    public void setClassNumber(int classNumber) { this.classNumber = classNumber; }

    public String getClassLetter() { return classLetter; }
    public void setClassLetter(String classLetter) { this.classLetter = classLetter; }

    public int getClassTeacherId() { return classTeacherId; }
    public void setClassTeacherId(int classTeacherId) { this.classTeacherId = classTeacherId; }

    public String getClassName() {
        return classNumber + " \"" + classLetter + "\"";
    }
}