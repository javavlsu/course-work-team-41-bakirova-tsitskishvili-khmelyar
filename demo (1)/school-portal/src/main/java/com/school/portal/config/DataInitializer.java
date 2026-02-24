package com.school.portal.config;

import com.school.portal.model.Role;
import com.school.portal.model.User;
import com.school.portal.repository.RoleRepository;
import com.school.portal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository,
                                          RoleRepository roleRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Проверяем, есть ли роли. Если нет - создаём
            // В SecurityConfig роли написаны на английском (TEACHER, DIRECTOR),
            // поэтому в базу кладём именно их
            if (roleRepository.count() == 0) {
                roleRepository.save(new Role("DIRECTOR"));
                roleRepository.save(new Role("TEACHER"));
                roleRepository.save(new Role("STUDENT"));
                roleRepository.save(new Role("PARENT"));
            }

            // 2. Проверяем, есть ли пользователи. Если нет - создаём
            if (userRepository.count() == 0) {

                // Достаем роли из базы, чтобы привязать их к пользователям
                Role directorRole = roleRepository.findByRoleName("DIRECTOR").get();
                Role teacherRole = roleRepository.findByRoleName("TEACHER").get();
                Role studentRole = roleRepository.findByRoleName("STUDENT").get();
                Role parentRole = roleRepository.findByRoleName("PARENT").get();

                // --- 1. ДИРЕКТОР ---
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

                // --- 2. УЧИТЕЛЬ 1 ---
                User teacher1 = new User();
                teacher1.setLogin("teacher1");
                teacher1.setPassword(passwordEncoder.encode("teacher"));
                teacher1.setFirstName("Алексей");
                teacher1.setLastName("Иванов");
                teacher1.setMiddleName("Петрович");
                teacher1.setRole(teacherRole);
                teacher1.setBirthDate(LocalDate.of(1980, 5, 15));
                teacher1.setEmail("teacher@school.ru");
                teacher1.setPhone("+79991234567");
                teacher1.setInfo("Классный руководитель 9А, преподаватель математики");
                teacher1.setCoins(0);
                userRepository.save(teacher1);

                // --- 3. УЧЕНИК 1 ---
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
                student1.setCoins(150); // Дадим ему немного монеток для теста :)
                userRepository.save(student1);

                // --- 4. УЧЕНИК 2 ---
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

                // --- 5. РОДИТЕЛЬ 1 ---
                User parent1 = new User();
                parent1.setLogin("parent1");
                parent1.setPassword(passwordEncoder.encode("parent"));
                parent1.setFirstName("Павел");
                parent1.setLastName("Смирнов");
                parent1.setMiddleName("Александрович");
                parent1.setRole(parentRole);
                parent1.setBirthDate(LocalDate.of(1982, 11, 5));
                parent1.setEmail("parent@mail.ru");
                parent1.setPhone("+79995554433");
                parent1.setInfo("Председатель родительского комитета");
                parent1.setCoins(0);
                userRepository.save(parent1);

            }
        };
    }
}