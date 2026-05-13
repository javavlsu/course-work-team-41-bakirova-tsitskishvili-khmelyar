-- Создание базы данных
CREATE DATABASE IF NOT EXISTS ShcED;
USE ShcED;

-- ---------------------------
--  Role
-- ---------------------------
CREATE TABLE IF NOT EXISTS Role (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL
);

-- ---------------------------
--  User
-- ---------------------------
CREATE TABLE User (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50),
    birth_date DATE,
    phone VARCHAR(20),
    email VARCHAR(100),
    login VARCHAR(100) NOT NULL,
    password VARCHAR(500) NOT NULL,
    info VARCHAR(500),
    FOREIGN KEY (role_id) REFERENCES Role(role_id)
);

-- ---------------------------
--  SchoolClass
-- ---------------------------
CREATE TABLE SchoolClass (
    class_id INT PRIMARY KEY AUTO_INCREMENT,
    class_number INT NOT NULL,
    class_letter CHAR(2) NOT NULL,
    class_teacher_id INT NOT NULL,
    FOREIGN KEY (class_teacher_id) REFERENCES User(user_id)
);

-- ---------------------------
--  Student_class
-- ---------------------------
CREATE TABLE Student_class (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    class_id INT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES User(user_id),
    FOREIGN KEY (class_id) REFERENCES SchoolClass(class_id)
);

-- ---------------------------
--  Subject
-- ---------------------------
CREATE TABLE Subject (
    subject_id INT PRIMARY KEY AUTO_INCREMENT,
    subject_name VARCHAR(100) NOT NULL
);

-- ---------------------------
--  Class_subject_teacher
-- ---------------------------
CREATE TABLE Class_subject_teacher (
    id INT PRIMARY KEY AUTO_INCREMENT,
    class_id INT NOT NULL,
    subject_id INT NOT NULL,
    teacher_id INT NOT NULL,
    FOREIGN KEY (class_id) REFERENCES SchoolClass(class_id),
    FOREIGN KEY (subject_id) REFERENCES Subject(subject_id),
    FOREIGN KEY (teacher_id) REFERENCES User(user_id)
);

-- ---------------------------
--  ScheduleTemplate
-- ---------------------------
CREATE TABLE ScheduleTemplate (
    id INT PRIMARY KEY AUTO_INCREMENT,
    class_id INT NOT NULL,
    subject_id INT NOT NULL,
    teacher_id INT NOT NULL,
    day_of_week TINYINT NOT NULL,
    lesson_number INT NOT NULL,
    room VARCHAR(20) NOT NULL,
    FOREIGN KEY (class_id) REFERENCES SchoolClass(class_id),
    FOREIGN KEY (subject_id) REFERENCES Subject(subject_id),
    FOREIGN KEY (teacher_id) REFERENCES User(user_id)
);

-- ---------------------------
--  Schedule
-- ---------------------------
CREATE TABLE Schedule (
    lesson_id INT PRIMARY KEY AUTO_INCREMENT,
    date DATE NOT NULL,
    lesson_number INT NOT NULL,
    class_id INT NOT NULL,
    subject_id INT NOT NULL,
    teacher_id INT NOT NULL,
    room VARCHAR(20) NOT NULL,
    lesson_topic VARCHAR(255),
    homework_text VARCHAR(500),
    FOREIGN KEY (class_id) REFERENCES SchoolClass(class_id),
    FOREIGN KEY (subject_id) REFERENCES Subject(subject_id),
    FOREIGN KEY (teacher_id) REFERENCES User(user_id)
);

-- ---------------------------
--  Homework
-- ---------------------------
CREATE TABLE Homework (
    homework_id INT PRIMARY KEY AUTO_INCREMENT,
    lesson_id INT NOT NULL,
    date DATE NOT NULL,
    text VARCHAR(1000),
    file_path BLOB,
    student_id INT NOT NULL,
    FOREIGN KEY (lesson_id) REFERENCES Schedule(lesson_id)
    FOREIGN KEY (student_id) REFERENCES User(user_id)
);

-- ---------------------------
--  Grade
-- ---------------------------
CREATE TABLE Grade (
    grade_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    homework_id INT,
    lesson_id INT NOT NULL,
    comment VARCHAR(100),
    date DATE NOT NULL,
    grade INT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES User(user_id),
    FOREIGN KEY (homework_id) REFERENCES Homework(homework_id),
    FOREIGN KEY (lesson_id) REFERENCES Schedule(lesson_id)
);

-- ---------------------------
--  Attendance
-- ---------------------------
CREATE TABLE Attendance (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    lesson_id INT NOT NULL,
    status CHAR(1) NOT NULL,
    FOREIGN KEY (student_id) REFERENCES User(user_id),
    FOREIGN KEY (lesson_id) REFERENCES Schedule(lesson_id)
);

-- ---------------------------
--  Remark
-- ---------------------------
CREATE TABLE Remark (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    lesson_id INT NOT NULL,
    text VARCHAR(1000) NOT NULL,
    FOREIGN KEY (student_id) REFERENCES User(user_id),
    FOREIGN KEY (lesson_id) REFERENCES Schedule(lesson_id)
);

-- ---------------------------
--  Announcement
-- ---------------------------
CREATE TABLE Announcement (
    announcement_id INT PRIMARY KEY AUTO_INCREMENT,
    class_id INT NOT NULL,
    created_at DATE NOT NULL,
    title VARCHAR(200) NOT NULL,
    text VARCHAR(1000) NOT NULL,
    FOREIGN KEY (class_id) REFERENCES SchoolClass(class_id)
);

-- ---------------------------
--  Student_parents
-- ---------------------------
CREATE TABLE Student_parents (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    parent_id INT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES User(user_id),
    FOREIGN KEY (parent_id) REFERENCES User(user_id)
);

-- ---------------------------
--  Message
-- ---------------------------
CREATE TABLE Message (
    message_id INT PRIMARY KEY AUTO_INCREMENT,
    sent_at DATE NOT NULL,
    from_user_id INT NOT NULL,
    to_user_id INT NOT NULL,
    message_text VARCHAR(1000) NOT NULL,
    FOREIGN KEY (from_user_id) REFERENCES User(user_id),
    FOREIGN KEY (to_user_id) REFERENCES User(user_id)
);

-- ---------------------------
--  Improvement
-- ---------------------------
CREATE TABLE Improvement (
    id INT PRIMARY KEY AUTO_INCREMENT,
    created_at DATE NOT NULL,
    from_user_id INT NOT NULL,
    title VARCHAR(200),
    text VARCHAR(1000) NOT NULL,
    FOREIGN KEY (from_user_id) REFERENCES User(user_id)
);

-- ---------------------------
--  Event
-- ---------------------------
CREATE TABLE Event (
    event_id INT PRIMARY KEY AUTO_INCREMENT,
    start_date DATE NOT NULL,
    end_date DATE,
    title VARCHAR(200) NOT NULL,
    text VARCHAR(1000) NOT NULL,
    file_path BLOB,
    main_organizer_id INT,
    FOREIGN KEY (main_organizer_id) REFERENCES User(user_id)
);
