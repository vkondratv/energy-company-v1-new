package com.energy_company_v1.service;

import com.energy_company_v1.model.ERole;
import com.energy_company_v1.model.Role;
import com.energy_company_v1.model.User;
import com.energy_company_v1.repository.RoleRepository;
import com.energy_company_v1.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Существующий метод регистрации
    public User registerUser(String username, String email, String password, Set<String> strRoles) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username уже занят");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email уже используется");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        Set<Role> roles = new HashSet<>();
        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Роль USER не найдена"));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Роль ADMIN не найдена"));
                        roles.add(adminRole);
                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Роль MODERATOR не найдена"));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Роль USER не найдена"));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }

    // ⭐ НОВЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ПРОФИЛЕМ ⭐

    /**
     * Найти пользователя по имени пользователя
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));
    }

    /**
     * Обновить данные пользователя
     */
    public User updateUser(User user) {
        // Проверяем, существует ли пользователь
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + user.getId()));

        // Проверяем уникальность email, если он изменился
        if (!existingUser.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email уже используется другим пользователем");
        }

        // Обновляем только разрешенные поля (пароль не обновляем здесь)
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());

        return userRepository.save(existingUser);
    }

    /**
     * Обновить пароль пользователя
     */
    public void updatePassword(String username, String currentPassword, String newPassword) {
        User user = findByUsername(username);

        // Проверяем текущий пароль
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Текущий пароль неверен");
        }

        // Обновляем пароль
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Получить всех пользователей (для админа)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Найти пользователя по ID
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + id));
    }

    /**
     * Удалить пользователя
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Пользователь не найден с ID: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Изменить роль пользователя (для админа)
     */
    public User changeUserRole(Long userId, Set<String> strRoles) {
        User user = findById(userId);

        Set<Role> roles = new HashSet<>();
        strRoles.forEach(role -> {
            switch (role.toLowerCase()) {
                case "admin":
                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Роль ADMIN не найдена"));
                    roles.add(adminRole);
                    break;
                case "mod":
                case "moderator":
                    Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                            .orElseThrow(() -> new RuntimeException("Роль MODERATOR не найдена"));
                    roles.add(modRole);
                    break;
                default:
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Роль USER не найдена"));
                    roles.add(userRole);
            }
        });

        user.setRoles(roles);
        return userRepository.save(user);
    }

    /**
     * Проверить, существует ли пользователь по email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Проверить, существует ли пользователь по username
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Получить количество пользователей
     */
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * Получить пользователя по email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}