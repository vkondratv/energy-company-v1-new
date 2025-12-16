package com.energy_company_v1.controller;

import com.energy_company_v1.model.User;
import com.energy_company_v1.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final UserService userService;

    // Добавляем конструктор с UserService
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showProfile(Authentication authentication, Model model) {
        try {
            // Получаем реального пользователя из базы данных
            User user = userService.findByUsername(authentication.getName());

            // Создаем DTO или передаем непосредственно пользователя
            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername());

            return "profile/profile";

        } catch (Exception e) {
            // Если произошла ошибка, используем старый подход с заглушкой
            model.addAttribute("username", authentication.getName());

            // Создаем простой объект с данными по умолчанию
            UserDto userDto = new UserDto();
            userDto.setUsername(authentication.getName());
            userDto.setEmail("Недоступно"); // Заглушка

            model.addAttribute("user", userDto);

            return "profile/profile";
        }
    }

    // Простой DTO (можно удалить, если используете реального User)
    public static class UserDto {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;

        // Геттеры и сеттеры
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}