package com.energy_company_v1.controller;

import com.energy_company_v1.model.User;
import com.energy_company_v1.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // Страница управления пользователями
    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    // Форма редактирования ролей пользователя
    @GetMapping("/users/edit/{id}")
    public String editUserRolesForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "admin/edit-roles";
    }

    // Обновление ролей пользователя
    @PostMapping("/users/update-roles/{id}")
    public String updateUserRoles(
            @PathVariable Long id,
            @RequestParam(value = "roles", required = false) Set<String> roles,
            RedirectAttributes redirectAttributes) {

        try {
            if (roles == null) {
                roles = new HashSet<>();
            }

            userService.changeUserRole(id, roles);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Роли пользователя успешно обновлены!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении ролей: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // Удаление пользователя
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Пользователь успешно удален!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении пользователя: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }
}