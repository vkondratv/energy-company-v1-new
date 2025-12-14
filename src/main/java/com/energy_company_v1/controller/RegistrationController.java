package com.energy_company_v1.controller;

import com.energy_company_v1.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.Set;

@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               @RequestParam(required = false) Set<String> roles,
                               Model model) {

        // Валидация
        if (username == null || username.trim().isEmpty()) {
            return "redirect:/register?error=Имя пользователя обязательно";
        }
        if (email == null || email.trim().isEmpty()) {
            return "redirect:/register?error=Email обязателен";
        }
        if (password == null || password.length() < 6) {
            return "redirect:/register?error=Пароль должен содержать минимум 6 символов";
        }
        if (!password.equals(confirmPassword)) {
            return "redirect:/register?error=Пароли не совпадают";
        }

        try {
            if (roles == null) {
                roles = new HashSet<>();
                roles.add("user");
            }

            userService.registerUser(username, email, password, roles);
            return "redirect:/login?success=Регистрация успешна! Теперь вы можете войти.";

        } catch (RuntimeException e) {
            return "redirect:/register?error=" + e.getMessage();
        }
    }
}