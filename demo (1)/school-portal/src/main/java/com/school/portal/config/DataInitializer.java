package com.school.portal.config;

import com.school.portal.model.*;
import com.school.portal.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    @Transactional
    public CommandLineRunner initDatabase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            SubjectRepository subjectRepository,
            SchoolClassRepository classRepository,
            StudentClassRepository studentClassRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                System.out.println("=== НАЧАЛО ИНИЦИАЛИЗАЦИИ ДАННЫХ ===");

                // Проверка подключения к БД
                try {
                    long count = roleRepository.count();
                    System.out.println("Подключение к БД успешно. Ролей в БД: " + count);
                } catch (DataAccessException e) {
                    System.err.println("ОШИБКА ПОДКЛЮЧЕНИЯ К БД: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }

                // 1. Создание ролей
                if (roleRepository.count() == 0) {
                    System.out.println("Создание ролей...");
                    Role directorRole = new Role("DIRECTOR");
                    Role teacherRole = new Role("TEACHER");
                    Role studentRole = new Role("STUDENT");
                    Role parentRole = new Role("PARENT");

                    roleRepository.save(directorRole);
                    roleRepository.save(teacherRole);
                    roleRepository.save(studentRole);
                    roleRepository.save(parentRole);

                    System.out.println("Роли созданы");
                } else {
                    System.out.println("Роли уже существуют");
                }

                // 2. Создание предметов
                if (subjectRepository.count() == 0) {
                    System.out.println("Создание предметов...");
                    subjectRepository.save(new Subject("Математика"));
                    subjectRepository.save(new Subject("Русский язык"));
                    subjectRepository.save(new Subject("Физика"));
                    subjectRepository.save(new Subject("Химия"));
                    subjectRepository.save(new Subject("История"));
                    subjectRepository.save(new Subject("Английский язык"));
                    subjectRepository.save(new Subject("Информатика"));
                    subjectRepository.save(new Subject("Биология"));
                    subjectRepository.save(new Subject("География"));
                    subjectRepository.save(new Subject("Литература"));
                    System.out.println("Предметы созданы");
                } else {
                    System.out.println("Предметы уже существуют");
                }

                // 3. Создание пользователей
                if (userRepository.count() == 0) {
                    System.out.println("Создание пользователей...");

                    // Получаем роли
                    Role directorRole = roleRepository.findByRoleName("DIRECTOR")
                            .orElseThrow(() -> new RuntimeException("Роль DIRECTOR не найдена"));
                    Role teacherRole = roleRepository.findByRoleName("TEACHER")
                            .orElseThrow(() -> new RuntimeException("Роль TEACHER не найдена"));
                    Role studentRole = roleRepository.findByRoleName("STUDENT")
                            .orElseThrow(() -> new RuntimeException("Роль STUDENT не найдена"));
                    Role parentRole = roleRepository.findByRoleName("PARENT")
                            .orElseThrow(() -> new RuntimeException("Роль PARENT не найдена"));

                    // Директор
                    User director = new User();
                    director.setLogin("director");
                    director.setPassword(passwordEncoder.encode("director"));
                    director.setFirstName("Мария");
                    director.setLastName("Петрова");
                    director.setMiddleName("Сергеевна");
                    director.setRole(directorRole);
                    director.setBirthDate(LocalDate.of(1975, 3, 22));
                    director.setEmail("director@school.ru");
                    director.setPhone("+79997654321");
                    director.setInfo("Директор школы, кандидат педагогических наук");
                    director.setCoins(0);
                    userRepository.save(director);
                    System.out.println("Создан директор: director");

                    // Учитель 1
                    User teacher1 = new User();
                    teacher1.setLogin("teacher1");
                    teacher1.setPassword(passwordEncoder.encode("teacher"));
                    teacher1.setFirstName("Алексей");
                    teacher1.setLastName("Иванов");
                    teacher1.setMiddleName("Петрович");
                    teacher1.setRole(teacherRole);
                    teacher1.setBirthDate(LocalDate.of(1980, 5, 15));
                    teacher1.setEmail("teacher1@school.ru");
                    teacher1.setPhone("+79991234567");
                    teacher1.setInfo("Классный руководитель 9А, преподаватель математики");
                    teacher1.setCoins(0);
                    userRepository.save(teacher1);
                    System.out.println("Создан учитель: teacher1");

                    // Учитель 2
                    User teacher2 = new User();
                    teacher2.setLogin("teacher2");
                    teacher2.setPassword(passwordEncoder.encode("teacher"));
                    teacher2.setFirstName("Елена");
                    teacher2.setLastName("Смирнова");
                    teacher2.setMiddleName("Дмитриевна");
                    teacher2.setRole(teacherRole);
                    teacher2.setBirthDate(LocalDate.of(1982, 8, 23));
                    teacher2.setEmail("teacher2@school.ru");
                    teacher2.setPhone("+79997654322");
                    teacher2.setInfo("Преподаватель русского языка и литературы");
                    teacher2.setCoins(0);
                    userRepository.save(teacher2);
                    System.out.println("Создан учитель: teacher2");

                    // Ученик 1
                    User student1 = new User();
                    student1.setLogin("student1");
                    student1.setPassword(passwordEncoder.encode("student"));
                    student1.setFirstName("Дмитрий");
                    student1.setLastName("Сидоров");
                    student1.setMiddleName("Иванович");
                    student1.setRole(studentRole);
                    student1.setBirthDate(LocalDate.of(2007, 8, 10));
                    student1.setEmail("student1@school.ru");
                    student1.setInfo("Успевающий ученик, участник олимпиад");
                    student1.setCoins(150);
                    userRepository.save(student1);
                    System.out.println("Создан ученик: student1");

                    // Ученик 2
                    User student2 = new User();
                    student2.setLogin("student2");
                    student2.setPassword(passwordEncoder.encode("student"));
                    student2.setFirstName("Анна");
                    student2.setLastName("Кузнецова");
                    student2.setMiddleName("Владимировна");
                    student2.setRole(studentRole);
                    student2.setBirthDate(LocalDate.of(2008, 2, 25));
                    student2.setEmail("student2@school.ru");
                    student2.setPhone("+79998887766");
                    student2.setInfo("Отличница, занимается музыкой");
                    student2.setCoins(200);
                    userRepository.save(student2);
                    System.out.println("Создан ученик: student2");

                    // Родитель
                    User parent1 = new User();
                    parent1.setLogin("parent1");
                    parent1.setPassword(passwordEncoder.encode("parent"));
                    parent1.setFirstName("Павел");
                    parent1.setLastName("Смирнов");
                    parent1.setMiddleName("Александрович");
                    parent1.setRole(parentRole);
                    parent1.setBirthDate(LocalDate.of(1982, 11, 5));
                    parent1.setEmail("parent1@mail.ru");
                    parent1.setPhone("+79995554433");
                    parent1.setInfo("Председатель родительского комитета");
                    parent1.setCoins(0);
                    userRepository.save(parent1);
                    System.out.println("Создан родитель: parent1");

                    System.out.println("Все пользователи созданы");
                } else {
                    System.out.println("Пользователи уже существуют");
                }

                // 4. Создание классов (только если пользователи есть)
                System.out.println("Проверка наличия пользователей...");
                System.out.println("Всего пользователей в БД: " + userRepository.count());

                // Проверяем каждого пользователя отдельно
                System.out.println("Поиск teacher1: " + userRepository.findByLogin("teacher1").isPresent());
                System.out.println("Поиск teacher2: " + userRepository.findByLogin("teacher2").isPresent());
                System.out.println("Поиск student1: " + userRepository.findByLogin("student1").isPresent());
                System.out.println("Поиск student2: " + userRepository.findByLogin("student2").isPresent());

                if (classRepository.count() == 0) {
                    System.out.println("Создание классов...");

                    // Получаем учителей
                    User teacher1 = userRepository.findByLogin("teacher1")
                            .orElseThrow(() -> new RuntimeException("Учитель teacher1 не найден"));
                    User teacher2 = userRepository.findByLogin("teacher2")
                            .orElseThrow(() -> new RuntimeException("Учитель teacher2 не найден"));

                    SchoolClass class9A = new SchoolClass();
                    class9A.setClassNumber(9);
                    class9A.setClassLetter("А");
                    class9A.setClassTeacher(teacher1);
                    classRepository.save(class9A);
                    System.out.println("Создан класс 9А");

                    SchoolClass class9B = new SchoolClass();
                    class9B.setClassNumber(9);
                    class9B.setClassLetter("Б");
                    class9B.setClassTeacher(teacher2);
                    classRepository.save(class9B);
                    System.out.println("Создан класс 9Б");

                    System.out.println("Классы созданы");
                } else {
                    System.out.println("Классы уже существуют");
                }

                // 5. Привязка учеников к классам
                if (studentClassRepository.count() == 0 && classRepository.count() > 0) {
                    System.out.println("Привязка учеников к классам...");

                    SchoolClass class9A = classRepository.findByClassNumberAndClassLetter(9, "А")
                            .orElseThrow(() -> new RuntimeException("Класс 9А не найден"));
                    SchoolClass class9B = classRepository.findByClassNumberAndClassLetter(9, "Б")
                            .orElseThrow(() -> new RuntimeException("Класс 9Б не найден"));

                    User student1 = userRepository.findByLogin("student1")
                            .orElseThrow(() -> new RuntimeException("Ученик student1 не найден"));
                    User student2 = userRepository.findByLogin("student2")
                            .orElseThrow(() -> new RuntimeException("Ученик student2 не найден"));

                    StudentClass sc1 = new StudentClass();
                    sc1.setStudent(student1);
                    sc1.setSchoolClass(class9A);
                    studentClassRepository.save(sc1);
                    System.out.println("Ученик student1 привязан к классу 9А");

                    StudentClass sc2 = new StudentClass();
                    sc2.setStudent(student2);
                    sc2.setSchoolClass(class9B);
                    studentClassRepository.save(sc2);
                    System.out.println("Ученик student2 привязан к классу 9Б");

                    System.out.println("Ученики привязаны к классам");
                } else {
                    System.out.println("Связи учеников с классами уже существуют или классы не созданы");
                }

                System.out.println("=== ИНИЦИАЛИЗАЦИЯ ДАННЫХ ЗАВЕРШЕНА ===");

            } catch (Exception e) {
                System.err.println("!!! ОШИБКА ПРИ ИНИЦИАЛИЗАЦИИ ДАННЫХ: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        };
    }
}