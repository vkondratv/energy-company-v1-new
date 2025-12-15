package com.energy_company_v1.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    @GetMapping("/profile")
    public String showProfile(Authentication authentication, Model model) {
        // Просто передаем имя пользователя
        model.addAttribute("username", authentication.getName());

        // Создаем простой объект пользователя
        UserDto user = new UserDto();
        user.setUsername(authentication.getName());
        user.setEmail("user@example.com"); // Заглушка

        model.addAttribute("user", user);

        return "profile/profile";
    }

    // Простой DTO для пользователя
    public static class UserDto {
        private String username;
        private String email;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}