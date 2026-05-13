package com.school.portal.controller;

import com.school.portal.model.User;
import com.school.portal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    // Этот метод будет невидимо срабатывать перед открытием КАЖДОЙ страницы.
    // Он положит переменную "currentUser" во все HTML-шаблоны.
    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Проверяем, что кто-то авторизован (и это не анонимный посетитель на странице логина)
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            // Достаем юзера со всеми его ФИО, ролью и монетками из БД
            return userRepository.findByLogin(auth.getName()).orElse(null);
        }
        return null;
    }
}