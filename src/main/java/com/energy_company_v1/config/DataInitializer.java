package com.energy_company_v1.config;

import com.energy_company_v1.model.*;
import com.energy_company_v1.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EnergyObjectRepository energyObjectRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           EnergyObjectRepository energyObjectRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.energyObjectRepository = energyObjectRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Создание ролей
        if (roleRepository.count() == 0) {
            Role roleUser = new Role();
            roleUser.setName(ERole.ROLE_USER);
            roleRepository.save(roleUser);

            Role roleModerator = new Role();
            roleModerator.setName(ERole.ROLE_MODERATOR);
            roleRepository.save(roleModerator);

            Role roleAdmin = new Role();
            roleAdmin.setName(ERole.ROLE_ADMIN);
            roleRepository.save(roleAdmin);
        }

        // Создание тестового администратора
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@energycompany.com");
            admin.setPassword(passwordEncoder.encode("admin123"));

            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Роль ADMIN не найдена"));

            // ИСПРАВЛЕНИЕ: используем Set вместо List
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);

            userRepository.save(admin);

            // Создание тестового модератора
            User moderator = new User();
            moderator.setUsername("moderator");
            moderator.setEmail("moderator@energycompany.com");
            moderator.setPassword(passwordEncoder.encode("mod123"));

            Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                    .orElseThrow(() -> new RuntimeException("Роль MODERATOR не найдена"));

            Set<Role> modRoles = new HashSet<>();
            modRoles.add(modRole);
            moderator.setRoles(modRoles);

            userRepository.save(moderator);

            // Создание тестового пользователя
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@energycompany.com");
            user.setPassword(passwordEncoder.encode("user123"));

            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Роль USER не найдена"));

            Set<Role> userRoles = new HashSet<>();
            userRoles.add(userRole);
            user.setRoles(userRoles);

            userRepository.save(user);
        }

        // Создание тестовых энергообъектов
        if (energyObjectRepository.count() == 0) {
            EnergyObject obj1 = new EnergyObject();
            obj1.setName("Ленинградская АЭС");
            obj1.setType("АЭС");
            obj1.setLocation("Ленинградская область");
            obj1.setPower(4200.0);
            obj1.setCommissioningYear(1973);
            obj1.setEfficiency(38.5);
            obj1.setActive(true);
            obj1.setLastMaintenanceDate(LocalDate.of(2023, 10, 15));
            obj1.setDescription("Крупнейшая атомная электростанция в Северо-Западном регионе");
            energyObjectRepository.save(obj1);

            EnergyObject obj2 = new EnergyObject();
            obj2.setName("Саратовская ГЭС");
            obj2.setType("ГЭС");
            obj2.setLocation("Саратовская область");
            obj2.setPower(1360.0);
            obj2.setCommissioningYear(1967);
            obj2.setEfficiency(85.0);
            obj2.setActive(true);
            obj2.setLastMaintenanceDate(LocalDate.of(2023, 8, 20));
            obj2.setDescription("Одна из крупнейших гидроэлектростанций на Волге");
            energyObjectRepository.save(obj2);

            EnergyObject obj3 = new EnergyObject();
            obj3.setName("Калининградская ТЭЦ-2");
            obj3.setType("ТЭЦ");
            obj3.setLocation("Калининград");
            obj3.setPower(450.0);
            obj3.setCommissioningYear(2005);
            obj3.setEfficiency(45.0);
            obj3.setActive(true);
            obj3.setLastMaintenanceDate(LocalDate.of(2023, 9, 10));
            obj3.setDescription("Теплоэлектроцентраль в Калининграде");
            energyObjectRepository.save(obj3);

            // Добавим еще несколько объектов для разнообразия
            EnergyObject obj4 = new EnergyObject();
            obj4.setName("Волгоградская ТЭЦ-3");
            obj4.setType("ТЭЦ");
            obj4.setLocation("Волгоград");
            obj4.setPower(985.0);
            obj4.setCommissioningYear(1985);
            obj4.setEfficiency(42.0);
            obj4.setActive(true);
            obj4.setLastMaintenanceDate(LocalDate.of(2023, 7, 5));
            energyObjectRepository.save(obj4);

            EnergyObject obj5 = new EnergyObject();
            obj5.setName("Солнечная станция 'Крымская'");
            obj5.setType("СЭС");
            obj5.setLocation("Крым");
            obj5.setPower(110.0);
            obj5.setCommissioningYear(2020);
            obj5.setEfficiency(22.5);
            obj5.setActive(true);
            obj5.setLastMaintenanceDate(LocalDate.of(2023, 6, 30));
            obj5.setDescription("Солнечная электростанция в Крыму");
            energyObjectRepository.save(obj5);

            EnergyObject obj6 = new EnergyObject();
            obj6.setName("Ветропарк 'Адыгейский'");
            obj6.setType("ВЭС");
            obj6.setLocation("Адыгея");
            obj6.setPower(150.0);
            obj6.setCommissioningYear(2021);
            obj6.setEfficiency(35.0);
            obj6.setActive(false);
            obj6.setLastMaintenanceDate(LocalDate.of(2023, 5, 15));
            obj6.setDescription("Ветровая электростанция на плановом ремонте");
            energyObjectRepository.save(obj6);
        }
    }
}