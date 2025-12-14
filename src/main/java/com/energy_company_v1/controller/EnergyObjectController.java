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
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {

        try {
            Page<EnergyObject> energyObjectsPage;

            if (keyword != null && !keyword.isEmpty()) {
                // Поиск по ключевому слову
                energyObjectsPage = energyObjectService.searchEnergyObjects(keyword,
                        PageRequest.of(page, size, Sort.by("id").descending()));
            } else {
                // Все объекты с пагинацией
                energyObjectsPage = energyObjectService.getAllEnergyObjects(
                        PageRequest.of(page, size, Sort.by("id").descending()));
            }

            // Логирование для отладки
            System.out.println("=== СПИСОК ОБЪЕКТОВ ===");
            System.out.println("Всего элементов: " + energyObjectsPage.getTotalElements());
            System.out.println("Страница: " + page + ", размер: " + size);
            System.out.println("Содержимое страницы: " + energyObjectsPage.getContent().size());
            energyObjectsPage.getContent().forEach(obj ->
                    System.out.println(" - " + obj.getId() + ": " + obj.getName()));

            // Добавляем в модель
            model.addAttribute("energyObjects", energyObjectsPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", energyObjectsPage.getTotalPages());
            model.addAttribute("totalItems", energyObjectsPage.getTotalElements());
            model.addAttribute("keyword", keyword);

        } catch (Exception e) {
            System.out.println("Ошибка при получении списка: " + e.getMessage());
            e.printStackTrace();

            // В случае ошибки возвращаем пустой список
            model.addAttribute("energyObjects", new ArrayList<EnergyObject>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0);
            model.addAttribute("keyword", keyword);
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
        if (result.hasErrors()) {
            // Если есть ошибки валидации
            return "energy-objects/create";
        }

        try {
            energyObjectService.createEnergyObject(energyObject);
            redirectAttributes.addFlashAttribute("success",
                    "Энергообъект успешно создан!");
        } catch (Exception e) {
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
    public String deleteEnergyObject(@PathVariable Long id, Model model) {
        try {
            energyObjectService.deleteEnergyObject(id);
            return "redirect:/energy-objects?success=Объект успешно удален";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Ошибка при удалении объекта: " + e.getMessage());
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
    @PreAuthorize("hasRole('ADMIN')")
    public String showStatistics(Model model) {
        try {
            // Получаем расширенную статистику
            Map<String, Object> statistics = energyObjectService.getStatistics();

            // Дополнительная статистика для нового дизайна
            long totalObjects = energyObjectService.getAllEnergyObjects().size();
            long activeObjects = energyObjectService.getAllEnergyObjects()
                    .stream()
                    .filter(EnergyObject::getActive)
                    .count();

            // Статистика по типам с процентами
            Map<String, Long> typeCounts = energyObjectService.getAllEnergyObjects()
                    .stream()
                    .collect(Collectors.groupingBy(
                            EnergyObject::getType,
                            Collectors.counting()
                    ));

            Map<String, Double> typePercentages = typeCounts.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> totalObjects > 0 ?
                                    (entry.getValue() * 100.0 / totalObjects) : 0
                    ));

            // Средний КПД по типам
            Map<String, Double> avgEfficiencyByType = energyObjectService.getAllEnergyObjects()
                    .stream()
                    .collect(Collectors.groupingBy(
                            EnergyObject::getType,
                            Collectors.averagingDouble(EnergyObject::getEfficiency)
                    ));

            model.addAttribute("statistics", statistics);
            model.addAttribute("totalObjects", totalObjects);
            model.addAttribute("activeObjects", activeObjects);
            model.addAttribute("typeCounts", typeCounts);
            model.addAttribute("typePercentages", typePercentages);
            model.addAttribute("avgEfficiencyByType", avgEfficiencyByType);

            return "energy-objects/statistics";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при получении статистики: " + e.getMessage());
            return "redirect:/energy-objects";
        }


    }
}