package com.energy_company_v1.controller;

import com.energy_company_v1.model.EnergyObject;
import com.energy_company_v1.repository.UserRepository;
import com.energy_company_v1.service.EnergyObjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/energy-objects")
public class EnergyObjectController {

    private final EnergyObjectService energyObjectService;
    private final UserRepository userRepository; // Добавляем репозиторий пользователей

    public EnergyObjectController(EnergyObjectService energyObjectService,
                                  UserRepository userRepository) { // Добавляем в конструктор
        this.energyObjectService = energyObjectService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listEnergyObjects(Model model,
                                    @RequestParam(required = false) String search, // Изменил keyword на search
                                    @RequestParam(defaultValue = "id") String sortBy,
                                    @RequestParam(defaultValue = "desc") String direction,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {

        try {
            Page<EnergyObject> energyObjectsPage;

            // Определяем направление сортировки
            Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                    ? Sort.Direction.ASC : Sort.Direction.DESC;

            // Создаем объект сортировки
            Sort sort = Sort.by(sortDirection, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Поиск с учетом ключевого слова (теперь search вместо keyword)
            if (search != null && !search.trim().isEmpty()) {
                energyObjectsPage = energyObjectService.searchEnergyObjects(search, pageable);
            } else {
                energyObjectsPage = energyObjectService.getAllEnergyObjects(pageable);
            }

            // Для отладки
            System.out.println("=== ПАРАМЕТРЫ ===");
            System.out.println("Поиск: " + search);
            System.out.println("Сортировка по: " + sortBy);
            System.out.println("Направление: " + direction);

            // Передаем данные в модель
            model.addAttribute("energyObjects", energyObjectsPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", energyObjectsPage.getTotalPages());
            model.addAttribute("totalItems", energyObjectsPage.getTotalElements());
            model.addAttribute("search", search);       // Изменил keyword на search
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("direction", direction);

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("energyObjects", Page.empty());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0);
            model.addAttribute("search", search);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("direction", direction);
        }

        return "energy-objects/list";
    }

    @GetMapping("/all")
    public ResponseEntity<List<EnergyObject>> getAllEnergyObjects() {
        try {
            List<EnergyObject> energyObjects = energyObjectService.getAllEnergyObjects();
            return ResponseEntity.ok(energyObjects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    // Остальные методы остаются без изменений...
    @GetMapping("/create")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("energyObject", new EnergyObject());
        return "energy-objects/create";
    }

    @PostMapping("/create")
    public String createEnergyObject(@ModelAttribute EnergyObject energyObject,
                                     BindingResult result,
                                     RedirectAttributes redirectAttributes) {

        System.out.println("=== POST /create ===");
        System.out.println("Объект из формы: " + energyObject.toString());

        if (result.hasErrors()) {
            System.out.println("Ошибки валидации: " + result.getAllErrors());
            return "energy-objects/create";
        }

        try {
            EnergyObject created = energyObjectService.createEnergyObject(energyObject);
            System.out.println("Успешно создан объект ID: " + created.getId());

            redirectAttributes.addFlashAttribute("success",
                    "Энергообъект '" + created.getName() + "' успешно создан!");
        } catch (Exception e) {
            System.out.println("Ошибка при создании: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при создании: " + e.getMessage());
        }

        return "redirect:/energy-objects";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            // Ваш метод возвращает EnergyObject, а не Optional
            EnergyObject energyObject = energyObjectService.getEnergyObjectById(id);
            model.addAttribute("energyObject", energyObject);
            return "energy-objects/edit";
        } catch (RuntimeException e) {
            // Если объект не найден, перенаправляем с сообщением об ошибке
            return "redirect:/energy-objects?error=Object+not+found";
        }
    }

    // Метод для обработки обновления
    @PostMapping("/update/{id}")
    public String updateEnergyObject(@PathVariable Long id,
                                     @ModelAttribute EnergyObject energyObject,
                                     RedirectAttributes redirectAttributes) {
        try {
            energyObjectService.updateEnergyObject(id, energyObject);
            redirectAttributes.addFlashAttribute("successMessage", "Энергообъект успешно обновлен!");
            return "redirect:/energy-objects";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении: " + e.getMessage());
            return "redirect:/energy-objects/edit/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public String deleteEnergyObject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            energyObjectService.deleteEnergyObject(id);
            redirectAttributes.addFlashAttribute("successMessage", "Энергообъект успешно удален!");
            return "redirect:/energy-objects";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении объекта: " + e.getMessage());
            return "redirect:/energy-objects";
        }
    }

    public Map<String, Object> getEnhancedStatistics() {
        ResponseEntity<List<EnergyObject>> response = getAllEnergyObjects();
        List<EnergyObject> allObjects = new ArrayList<>();
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            allObjects = response.getBody();
        }
        long totalObjects = allObjects.size();

        if (totalObjects == 0) {
            return Map.of(
                    "totalObjects", 0L,
                    "activeObjects", 0L,
                    "inactiveObjects", 0L,
                    "totalPower", 0.0,
                    "averagePower", 0.0,
                    "averageEfficiency", 0.0,
                    "typeDistribution", Map.of(),
                    "powerByType", Map.of(),
                    "efficiencyByType", Map.of()
            );
        }

        // Базовая статистика
        long activeObjects = allObjects.stream()
                .filter(EnergyObject::getActive)
                .count();
        long inactiveObjects = totalObjects - activeObjects;

        Double totalPower = allObjects.stream()
                .mapToDouble(EnergyObject::getPower)
                .sum();

        Double averagePower = totalPower / totalObjects;

        Double averageEfficiency = allObjects.stream()
                .mapToDouble(EnergyObject::getEfficiency)
                .average()
                .orElse(0.0);

        // Распределение по типам
        Map<String, Long> typeDistribution = allObjects.stream()
                .collect(Collectors.groupingBy(
                        EnergyObject::getType,
                        Collectors.counting()
                ));

        Map<String, Double> typePercentages = typeDistribution.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Math.round((entry.getValue() * 100.0 / totalObjects) * 10.0) / 10.0
                ));

        // Мощность по типам
        Map<String, Double> powerByType = allObjects.stream()
                .collect(Collectors.groupingBy(
                        EnergyObject::getType,
                        Collectors.summingDouble(EnergyObject::getPower)
                ));

        // Средний КПД по типам
        Map<String, Double> efficiencyByType = allObjects.stream()
                .collect(Collectors.groupingBy(
                        EnergyObject::getType,
                        Collectors.averagingDouble(EnergyObject::getEfficiency)
                ));

        // Самый старый и самый новый объекты
        EnergyObject oldestObject = allObjects.stream()
                .min((o1, o2) -> o1.getCommissioningYear().compareTo(o2.getCommissioningYear()))
                .orElse(null);

        EnergyObject newestObject = allObjects.stream()
                .max((o1, o2) -> o1.getCommissioningYear().compareTo(o2.getCommissioningYear()))
                .orElse(null);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalObjects", totalObjects);
        stats.put("activeObjects", activeObjects);
        stats.put("inactiveObjects", inactiveObjects);
        stats.put("totalPower", Math.round(totalPower * 100.0) / 100.0);
        stats.put("averagePower", Math.round(averagePower * 100.0) / 100.0);
        stats.put("averageEfficiency", Math.round(averageEfficiency * 10.0) / 10.0);
        stats.put("typeDistribution", typeDistribution);
        stats.put("typePercentages", typePercentages);
        stats.put("powerByType", powerByType);
        stats.put("efficiencyByType", efficiencyByType);
        stats.put("oldestObject", oldestObject);
        stats.put("newestObject", newestObject);
        stats.put("activePercentage", Math.round((activeObjects * 100.0 / totalObjects) * 10.0) / 10.0);
        return stats;
    }
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    public String showStatistics(Model model) {
        try {
            // Получаем все объекты
            List<EnergyObject> allObjects = energyObjectService.getAllEnergyObjects();

            if (allObjects.isEmpty()) {
                // Если нет объектов, возвращаем пустую статистику
                model.addAttribute("errorMessage", "Нет данных для отображения статистики");
                return "energy-objects/statistics";
            }

            long totalObjects = allObjects.size();
            long activeObjects = allObjects.stream()
                    .filter(EnergyObject::getActive)
                    .count();
            long inactiveObjects = totalObjects - activeObjects;

            // Вычисляем проценты
            double activePercentage = (totalObjects > 0) ?
                    Math.round((activeObjects * 100.0 / totalObjects) * 10.0) / 10.0 : 0.0;
            double inactivePercentage = (totalObjects > 0) ?
                    Math.round((inactiveObjects * 100.0 / totalObjects) * 10.0) / 10.0 : 0.0;

            // Статистика по типам с процентами
            Map<String, Long> typeCounts = allObjects.stream()
                    .collect(Collectors.groupingBy(
                            EnergyObject::getType,
                            Collectors.counting()
                    ));

            Map<String, Double> typePercentages = new HashMap<>();
            for (Map.Entry<String, Long> entry : typeCounts.entrySet()) {
                double percentage = Math.round((entry.getValue() * 100.0 / totalObjects) * 10.0) / 10.0;
                typePercentages.put(entry.getKey(), percentage);
            }

            // Мощность по типам
            Map<String, Double> powerByType = allObjects.stream()
                    .collect(Collectors.groupingBy(
                            EnergyObject::getType,
                            Collectors.summingDouble(EnergyObject::getPower)
                    ));

            // Средний КПД по типам
            Map<String, Double> avgEfficiencyByType = allObjects.stream()
                    .collect(Collectors.groupingBy(
                            EnergyObject::getType,
                            Collectors.averagingDouble(EnergyObject::getEfficiency)
                    ));

            // Общая и средняя мощность
            double totalPower = allObjects.stream()
                    .mapToDouble(EnergyObject::getPower)
                    .sum();
            double averagePower = Math.round((totalPower / totalObjects) * 100.0) / 100.0;

            // Средний КПД
            double averageEfficiency = allObjects.stream()
                    .mapToDouble(EnergyObject::getEfficiency)
                    .average()
                    .orElse(0.0);

            // Мощность активных объектов
            double totalActivePower = allObjects.stream()
                    .filter(EnergyObject::getActive)
                    .mapToDouble(EnergyObject::getPower)
                    .sum();

            // Передаем данные в модель
            model.addAttribute("totalObjects", totalObjects);
            model.addAttribute("activeObjects", activeObjects);
            model.addAttribute("inactiveObjects", inactiveObjects);
            model.addAttribute("activePercentage", activePercentage);
            model.addAttribute("inactivePercentage", inactivePercentage);
            model.addAttribute("typeCounts", typeCounts);
            model.addAttribute("typePercentages", typePercentages);
            model.addAttribute("powerByType", powerByType);
            model.addAttribute("avgEfficiencyByType", avgEfficiencyByType);
            model.addAttribute("totalPower", Math.round(totalPower * 100.0) / 100.0);
            model.addAttribute("averagePower", averagePower);
            model.addAttribute("averageEfficiency", Math.round(averageEfficiency * 10.0) / 10.0);
            model.addAttribute("totalActivePower", Math.round(totalActivePower * 100.0) / 100.0);

            return "energy-objects/statistics";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при получении статистики: " + e.getMessage());
            return "redirect:/energy-objects";
        }


    }
}