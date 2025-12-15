package com.energy_company_v1.controller;

import com.energy_company_v1.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

    private final UserRepository userRepository;

    public MainController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Добавляем данные для отображения в футере
        long userCount = userRepository.count();
        model.addAttribute("userCount", userCount);
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "success", required = false) String success,
                        Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "Неверное имя пользователя или пароль");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Вы успешно вышли из системы");
        }
        if (success != null) {
            model.addAttribute("successMessage", success);
        }

        return "login";
    }

    @GetMapping("/register")
    public String register(@RequestParam(value = "error", required = false) String error,
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


    @GetMapping("/about")
    public String aboutPage(Model model) {
            // Данные об авторе
        model.addAttribute("authorName", "Владислав Кондратьев");
        model.addAttribute("authorInfo", "Разработчик информационных систем");
        model.addAttribute("contactEmail", "vkondratv@example.com");
        model.addAttribute("projectDescription",
                    "Информационно-справочная система для управления энергетическими объектами компании");

            // Технологии
        model.addAttribute("technologies", new String[] {
                    "Spring Boot 3.1+",
                    "Spring Security 6+",
                    "Spring Data JPA",
                    "Thymeleaf",
                    "Bootstrap 5",
                    "PostgreSQL/MySQL"
        });

        return "about"; // Возвращает шаблон about.html
    }


    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("errorMessage", "У вас нет прав для доступа к этой странице");
        return "access-denied";
    }
}