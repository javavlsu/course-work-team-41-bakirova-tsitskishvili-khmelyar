package com.school.portal.service;

import com.school.portal.model.User;
import com.school.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Transactional
    public User createUser(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    public String generateLogin(String firstName, String lastName) {
        String baseLogin = (firstName != null && !firstName.isEmpty() ?
                firstName.toLowerCase().charAt(0) : "") +
                (lastName != null ? lastName.toLowerCase() : "");

        String login = baseLogin;
        int counter = 1;

        while (userRepository.findByLogin(login).isPresent()) {
            login = baseLogin + counter;
            counter++;
        }

        return login;
    }

    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    public List<User> getUsersByRole(String roleName) {
        return userRepository.findByRole_RoleName(roleName);
    }
}