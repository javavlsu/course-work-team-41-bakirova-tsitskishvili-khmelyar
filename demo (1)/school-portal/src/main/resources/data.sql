-- Очистка существующих данных
DELETE FROM grades;
DELETE FROM homework;
DELETE FROM attendance;
DELETE FROM messages;
DELETE FROM announcements;
DELETE FROM schedule;
DELETE FROM class_subject_teacher;
DELETE FROM student_class;
DELETE FROM classes;
DELETE FROM subjects;
DELETE FROM users;
DELETE FROM roles;

-- Сброс последовательностей
ALTER TABLE roles ALTER COLUMN role_id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE subjects ALTER COLUMN subject_id RESTART WITH 1;
ALTER TABLE classes ALTER COLUMN class_id RESTART WITH 1;
ALTER TABLE schedule ALTER COLUMN lesson_id RESTART WITH 1;
ALTER TABLE grades ALTER COLUMN grade_id RESTART WITH 1;

-- Роли
INSERT INTO roles (role_name) VALUES ('DIRECTOR');
INSERT INTO roles (role_name) VALUES ('TEACHER');
INSERT INTO roles (role_name) VALUES ('STUDENT');
INSERT INTO roles (role_name) VALUES ('PARENT');

-- Предметы
INSERT INTO subjects (subject_name) VALUES ('Математика');
INSERT INTO subjects (subject_name) VALUES ('Русский язык');
INSERT INTO subjects (subject_name) VALUES ('Физика');
INSERT INTO subjects (subject_name) VALUES ('Химия');
INSERT INTO subjects (subject_name) VALUES ('История');
INSERT INTO subjects (subject_name) VALUES ('Английский язык');
INSERT INTO subjects (subject_name) VALUES ('Информатика');
INSERT INTO subjects (subject_name) VALUES ('Биология');
INSERT INTO subjects (subject_name) VALUES ('География');
INSERT INTO subjects (subject_name) VALUES ('Литература');

-- Пользователи будут созданы через DataInitializer с зашифрованными паролями
-- Но добавим дополнительные тестовые данные через SQL после того, как пользователи созданы