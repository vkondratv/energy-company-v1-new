package com.energy_company_v1.controller;

import com.energy_company_v1.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register-form")
    public String showRegisterForm(@RequestParam(value = "error", required = false) String error,
                                   @RequestParam(value = "success", required = false) String success,
                                   Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        if (success != null) {
            model.addAttribute("successMessage", success);
        }

        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               @RequestParam(required = false) Set<String> roles,
                               Model model) {

        String errorMessage = null;

        // Валидация
        if (username == null || username.trim().isEmpty()) {
            errorMessage = "Имя пользователя обязательно";
        } else if (email == null || email.trim().isEmpty()) {
            errorMessage = "Email обязателен";
        } else if (password == null || password.length() < 6) {
            errorMessage = "Пароль должен содержать минимум 6 символов";
        } else if (!password.equals(confirmPassword)) {
            errorMessage = "Пароли не совпадают";
        }

        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            return "register";
        }

        try {
            if (roles == null) {
                roles = new HashSet<>();
                roles.add("user");
            }

            userService.registerUser(username, email, password, roles);

            // Используем английское сообщение или кодируем русское
            String encodedSuccess = URLEncoder.encode("Registration successful! You can now log in.",
                    StandardCharsets.UTF_8);
            return "redirect:/login?success=registration_success";

        } catch (RuntimeException e) {
            // Кодируем сообщение об ошибке
            String encodedError = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }
}