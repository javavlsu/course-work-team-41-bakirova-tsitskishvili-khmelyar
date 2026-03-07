package com.school.portal.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private Integer userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RoleId", nullable = false)
    private Role role;

    @Column(name = "LastName", nullable = false, length = 50)
    private String lastName;

    @Column(name = "FirstName", nullable = false, length = 50)
    private String firstName;

    @Column(name = "MiddleName", length = 50)
    private String middleName;

    @Column(name = "BirthDate")
    private LocalDate birthDate;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "Login", nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "Password", nullable = false)
    private String password;

    @Column(name = "Info", columnDefinition = "TEXT")
    private String info;

    @Column(name = "Coins", nullable = false)
    private Integer coins = 0;

    // Связи
    @OneToMany(mappedBy = "teacher")
    private Set<Schedule> schedules;

    @OneToMany(mappedBy = "teacher")
    private Set<ScheduleTemplate> scheduleTemplates;

    @OneToMany(mappedBy = "teacher")
    private Set<ClassSubjectTeacher> classSubjectTeachers;

    @OneToMany(mappedBy = "student")
    private Set<Homework> homeworks;

    @OneToMany(mappedBy = "student")
    private Set<Grade> grades;

    @OneToMany(mappedBy = "student")
    private Set<Remark> remarks;

    @OneToMany(mappedBy = "student")
    private Set<Attendance> attendances;

    @OneToMany(mappedBy = "fromUser")
    private Set<Message> sentMessages;

    @OneToMany(mappedBy = "toUser")
    private Set<Message> receivedMessages;

    @OneToMany(mappedBy = "classTeacher")
    private Set<SchoolClass> supervisedClasses;

    @OneToMany(mappedBy = "student")
    private Set<StudentClass> studentClasses;

    @OneToMany(mappedBy = "student")
    private Set<StudentParent> parentLinks;

    @OneToMany(mappedBy = "parent")
    private Set<StudentParent> studentLinks;

    @OneToMany(mappedBy = "student")
    private Set<MerchRequest> merchRequests;

    @OneToMany(mappedBy = "student")
    private Set<TransactionHistory> transactions;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

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

    public Integer getCoins() { return coins; }
    public void setCoins(Integer coins) { this.coins = coins; }

    public String getFullName() {
        return lastName + " " + firstName + (middleName != null ? " " + middleName : "");
    }

    // Геттеры и сеттеры для связей
    public Set<Schedule> getSchedules() { return schedules; }
    public void setSchedules(Set<Schedule> schedules) { this.schedules = schedules; }

    public Set<ScheduleTemplate> getScheduleTemplates() { return scheduleTemplates; }
    public void setScheduleTemplates(Set<ScheduleTemplate> scheduleTemplates) { this.scheduleTemplates = scheduleTemplates; }

    public Set<ClassSubjectTeacher> getClassSubjectTeachers() { return classSubjectTeachers; }
    public void setClassSubjectTeachers(Set<ClassSubjectTeacher> classSubjectTeachers) { this.classSubjectTeachers = classSubjectTeachers; }

    public Set<Homework> getHomeworks() { return homeworks; }
    public void setHomeworks(Set<Homework> homeworks) { this.homeworks = homeworks; }

    public Set<Grade> getGrades() { return grades; }
    public void setGrades(Set<Grade> grades) { this.grades = grades; }

    public Set<Remark> getRemarks() { return remarks; }
    public void setRemarks(Set<Remark> remarks) { this.remarks = remarks; }

    public Set<Attendance> getAttendances() { return attendances; }
    public void setAttendances(Set<Attendance> attendances) { this.attendances = attendances; }

    public Set<Message> getSentMessages() { return sentMessages; }
    public void setSentMessages(Set<Message> sentMessages) { this.sentMessages = sentMessages; }

    public Set<Message> getReceivedMessages() { return receivedMessages; }
    public void setReceivedMessages(Set<Message> receivedMessages) { this.receivedMessages = receivedMessages; }

    public Set<SchoolClass> getSupervisedClasses() { return supervisedClasses; }
    public void setSupervisedClasses(Set<SchoolClass> supervisedClasses) { this.supervisedClasses = supervisedClasses; }

    public Set<StudentClass> getStudentClasses() { return studentClasses; }
    public void setStudentClasses(Set<StudentClass> studentClasses) { this.studentClasses = studentClasses; }

    public Set<StudentParent> getParentLinks() { return parentLinks; }
    public void setParentLinks(Set<StudentParent> parentLinks) { this.parentLinks = parentLinks; }

    public Set<StudentParent> getStudentLinks() { return studentLinks; }
    public void setStudentLinks(Set<StudentParent> studentLinks) { this.studentLinks = studentLinks; }

    public Set<MerchRequest> getMerchRequests() { return merchRequests; }
    public void setMerchRequests(Set<MerchRequest> merchRequests) { this.merchRequests = merchRequests; }

    public Set<TransactionHistory> getTransactions() { return transactions; }
    public void setTransactions(Set<TransactionHistory> transactions) { this.transactions = transactions; }
}