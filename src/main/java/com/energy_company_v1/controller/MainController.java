package com.energy_company_v1.controller;

import com.energy_company_v1.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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
            String decodedError = URLDecoder.decode(error, StandardCharsets.UTF_8);
            model.addAttribute("errorMessage", decodedError);
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Вы успешно вышли из системы");
        }
        if (success != null) {
            String decodedSuccess = URLDecoder.decode(success, StandardCharsets.UTF_8);
            // Изменяем здесь:
            if ("registration_success".equals(success)) {
                model.addAttribute("successMessage", "Регистрация успешна!");
            } else {
                model.addAttribute("successMessage", decodedSuccess);
            }
        }

        return "login";
    }

    @GetMapping("/register")
    public String register(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "success", required = false) String success,
                           Model model) {

        if (error != null) {
            // Декодируем параметр, если он закодирован
            String decodedError = URLDecoder.decode(error, StandardCharsets.UTF_8);
            model.addAttribute("errorMessage", decodedError);
        }
        if (success != null) {
            // Декодируем параметр, если он закодирован
            String decodedSuccess = URLDecoder.decode(success, StandardCharsets.UTF_8);
            model.addAttribute("successMessage", decodedSuccess);
        }
        if ("registration_success".equals(success)) {
            model.addAttribute("successMessage", "Регистрация успешна! Теперь вы можете войти.");
        }

        return "register";
    }

    @GetMapping("/about")
    public String aboutPage(Model model) {
        // Данные об авторе
        model.addAttribute("authorName", "Кондратюк Владислав Викторович");
        model.addAttribute("authorInfo", "Студент-разработчик");
        model.addAttribute("contactEmail", "superioriteee@yandex.ru");
        model.addAttribute("educationalInstitution", "Финансовый университет при Правительстве РФ");
        model.addAttribute("group", "ДПИ23-1, Прикладная информатика");
        model.addAttribute("location", "Сургут, Россия");
        model.addAttribute("projectStartDate", "25.10.2025");
        model.addAttribute("projectEndDate", "18.12.2025");
        model.addAttribute("projectDuration", "~2 месяца");

        // Технологии
        model.addAttribute("technologies", new String[] {
                "Spring Boot",
                "Spring Security",
                "Spring Data JPA",
                "Thymeleaf",
                "Bootstrap 5",
                "PostgreSQL"
        });

        return "about";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("errorMessage", "У вас нет прав для доступа к этой странице");
        return "access-denied";
    }
}