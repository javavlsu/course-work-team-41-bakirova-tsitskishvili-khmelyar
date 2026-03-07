package com.school.portal.model.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProfileViewModel {
    private Integer userId;
    private String lastName;
    private String firstName;
    private String middleName;
    private String login;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String roleName;
    private String info;
    private String classInfo;
    private String studentInfo;
    private Integer coins;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    public String getClassInfo() { return classInfo; }
    public void setClassInfo(String classInfo) { this.classInfo = classInfo; }

    public String getStudentInfo() { return studentInfo; }
    public void setStudentInfo(String studentInfo) { this.studentInfo = studentInfo; }

    public Integer getCoins() { return coins; }
    public void setCoins(Integer coins) { this.coins = coins; }

    public String getFullName() {
        return lastName + " " + firstName + (middleName != null ? " " + middleName : "");
    }

    public String getFormattedBirthDate() {
        if (birthDate != null) {
            return birthDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }
        return "Не указана";
    }
}