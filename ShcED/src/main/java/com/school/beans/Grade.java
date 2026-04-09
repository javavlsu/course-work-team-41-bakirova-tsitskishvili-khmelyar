package main.java.com.school.beans;

import java.sql.Date;

public class Grade {
    private int gradeId;
    private int studentId;
    private Integer homeworkId;
    private int lessonId;
    private String comment;
    private Date date;
    private int grade;

    public Grade() {}

    public Grade(int gradeId, int studentId, Integer homeworkId, int lessonId, String comment, Date date, int grade) {
        this.gradeId = gradeId;
        this.studentId = studentId;
        this.homeworkId = homeworkId;
        this.lessonId = lessonId;
        this.comment = comment;
        this.date = date;
        this.grade = grade;
    }

    public Grade(int studentId, int lessonId, Date date, int grade) {
        this(0, studentId, null, lessonId, null, date, grade);
    }

    public int getGradeId() { return gradeId; }
    public void setGradeId(int gradeId) { this.gradeId = gradeId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public Integer getHomeworkId() { return homeworkId; }
    public void setHomeworkId(Integer homeworkId) { this.homeworkId = homeworkId; }

    public int getLessonId() { return lessonId; }
    public void setLessonId(int lessonId) { this.lessonId = lessonId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }

    @Override
    public String toString() {
        return "Grade{gradeId=" + gradeId + ", studentId=" + studentId + ", grade=" + grade + "}";
    }
}