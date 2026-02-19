package com.school.portal.model;

import java.time.LocalDate;

public class User {
    private int userId;
    private String username;
    private String firstName;
    private String lastName;
    private String middleName;
    private String role;
    private int roleId;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String info;

    public User() {}

    public User(int userId, String username, String firstName, String lastName,
                String middleName, String role, int roleId) {
        this(userId, username, firstName, lastName, middleName, role, roleId,
                null, null, null, null);
    }

    public User(int userId, String username, String firstName, String lastName,
                String middleName, String role, int roleId, String email,
                String phone, LocalDate birthDate, String info) {
        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.role = role;
        this.roleId = roleId;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.info = info;
    }

    // Добавьте геттеры и сеттеры для новых полей:
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }


    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getFullName() {
        return lastName + " " + firstName + " " + (middleName != null ? middleName : "");
    }

    public String getShortName() {
        return lastName + " " + firstName.charAt(0) + ".";
    }
}