package com.school.portal.config;

import com.school.portal.model.*;
import com.school.portal.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            SubjectRepository subjectRepository,
            SchoolClassRepository classRepository,
            StudentClassRepository studentClassRepository,
            GradeRepository gradeRepository,
            ScheduleRepository scheduleRepository,
            PasswordEncoder passwordEncoder,
            MerchItemRepository merchItemRepository) {
        return args -> {
            System.out.println("=== НАЧАЛО ИНИЦИАЛИЗАЦИИ ДАННЫХ ===");

            // Получаем роли
            Role directorRole = roleRepository.findByRoleName("DIRECTOR")
                    .orElseGet(() -> roleRepository.save(new Role("DIRECTOR")));
            Role teacherRole = roleRepository.findByRoleName("TEACHER")
                    .orElseGet(() -> roleRepository.save(new Role("TEACHER")));
            Role studentRole = roleRepository.findByRoleName("STUDENT")
                    .orElseGet(() -> roleRepository.save(new Role("STUDENT")));
            Role parentRole = roleRepository.findByRoleName("PARENT")
                    .orElseGet(() -> roleRepository.save(new Role("PARENT")));

            // Создание пользователей (только если их нет)
            if (userRepository.count() == 0) {
                System.out.println("Создание пользователей...");

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
                parent1.setEmail("parent1@school.ru");
                parent1.setPhone("+79995554433");
                parent1.setInfo("Председатель родительского комитета");
                parent1.setCoins(0);
                userRepository.save(parent1);
                System.out.println("Создан родитель: parent1");

                System.out.println("Все пользователи созданы");
            }

            // Создание классов (только если их нет)
            if (classRepository.count() == 0) {
                System.out.println("Создание классов...");

                User teacher1 = userRepository.findByLogin("teacher1")
                        .orElseThrow(() -> new RuntimeException("Учитель teacher1 не найден"));
                User teacher2 = userRepository.findByLogin("teacher2")
                        .orElseThrow(() -> new RuntimeException("Учитель teacher2 не найден"));

                SchoolClass class9A = new SchoolClass();
                class9A.setClassNumber(9);
                class9A.setClassLetter("А");
                class9A.setClassTeacher(teacher1);
                classRepository.save(class9A);

                SchoolClass class9B = new SchoolClass();
                class9B.setClassNumber(9);
                class9B.setClassLetter("Б");
                class9B.setClassTeacher(teacher2);
                classRepository.save(class9B);

                System.out.println("Классы созданы");
            }

            // Привязка учеников к классам (только если их нет)
            if (studentClassRepository.count() == 0) {
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

                StudentClass sc2 = new StudentClass();
                sc2.setStudent(student2);
                sc2.setSchoolClass(class9B);
                studentClassRepository.save(sc2);

                System.out.println("Ученики привязаны к классам");
            }

            // Создание товаров (только если их нет)
            if (merchItemRepository.count() == 0) {
                System.out.println("Создание товаров в магазине мерча...");

                MerchItem pen = new MerchItem();
                pen.setName("Фирменная ручка");
                pen.setText("Стильная синяя шариковая ручка. Приносит удачу на контрольных!");
                pen.setPrice(50);
                merchItemRepository.save(pen);

                MerchItem notebook = new MerchItem();
                notebook.setName("Блокнот");
                notebook.setText("Блокнот формата А6 для черновиков, коротких записей и прочего.");
                notebook.setPrice(150);
                notebook.setImageUrl("/img/notebook.jpg");
                merchItemRepository.save(notebook);

                MerchItem hoodie = new MerchItem();
                hoodie.setName("Худи");
                hoodie.setText("Теплое и уютное худи для прохладного времени года.");
                hoodie.setPrice(1000);
                hoodie.setImageUrl("/img/hoodie.jpg");
                merchItemRepository.save(hoodie);

                System.out.println("Товары для магазина мерча добавлены");
            }

            System.out.println("=== ИНИЦИАЛИЗАЦИЯ ДАННЫХ ЗАВЕРШЕНА ===");
        };
    }
}