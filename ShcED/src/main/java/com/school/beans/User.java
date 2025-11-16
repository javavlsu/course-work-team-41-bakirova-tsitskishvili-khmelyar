package main.java.com.school.beans;

import java.sql.Date;

public class User {
    private int userId;
    private int roleId;
    private String lastName;
    private String firstName;
    private String middleName;
    private Date birthDate;
    private String phone;
    private String email;
    private String login;
    private String password;
    private String info;

    public User() {}

    public User(int userId, int roleId, String lastName, String firstName, String middleName,
                Date birthDate, String phone, String email, String login, String password, String info) {
        this.userId = userId;
        this.roleId = roleId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.birthDate = birthDate;
        this.phone = phone;
        this.email = email;
        this.login = login;
        this.password = password;
        this.info = info;
    }

    public User(int roleId, String lastName, String firstName, String login, String password) {
        this(0, roleId, lastName, firstName, null, null, null, null, login, password, null);
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", name='" + lastName + " " + firstName + "', login='" + login + "'}";
    }
}