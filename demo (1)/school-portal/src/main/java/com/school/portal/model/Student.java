package com.school.portal.model;

// Модель для ученика
public class Student {
    private int userId;
    private String firstName;
    private String lastName;
    private String middleName;

    public Student() {}

    public Student(int userId, String lastName, String firstName, String middleName) {
        this.userId = userId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getFullName() {
        return lastName + " " + firstName + " " + (middleName != null ? middleName : "");
    }

    public String getShortName() {
        return lastName + " " + firstName.charAt(0) + ".";
    }
}