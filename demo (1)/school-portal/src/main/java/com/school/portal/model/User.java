package com.school.portal.model;

import jakarta.persistence.*; // Импортируем аннотации JPA
import java.time.LocalDate;

@Entity // Класс - таблица в БД
@Table(name = "users") // Название таблицы
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Автоинкремент
    @Column(name = "user_id")
    private int userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String phone;
    private String email;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "TEXT")
    private String info;

    @Column(nullable = false)
    private int coins = 0;

    public User() {}

    // Геттеры и сеттеры
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

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

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public String getFullName() {
        return lastName + " " + firstName + " " + (middleName != null ? middleName : "");
    }

    public String getShortName() {
        return lastName + " " + firstName.charAt(0) + ".";
    }
}