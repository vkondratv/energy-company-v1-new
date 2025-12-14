package com.energy_company_v1.controller;

import com.energy_company_v1.model.EnergyObject;
import com.energy_company_v1.repository.UserRepository;
import com.energy_company_v1.service.EnergyObjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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
    public String listEnergyObjects(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {

        try {
            // 1. Создаем Pageable для пагинации и сортировки
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(sortDirection, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<EnergyObject> energyObjectPage;

            // 2. Обрабатываем поиск
            if (search != null && !search.trim().isEmpty()) {
                List<EnergyObject> searchResults = energyObjectService.searchEnergyObjects(search);

                // Конвертируем List в Page для пагинации
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), searchResults.size());
                List<EnergyObject> pageContent = searchResults.subList(start, end);

                energyObjectPage = new PageImpl<>(
                        pageContent,
                        pageable,
                        searchResults.size()
                );
                model.addAttribute("search", search);
            } else {
                energyObjectPage = energyObjectService.getAllEnergyObjects(pageable);
            }

            // 3. Получаем дополнительные данные для футера и статистики
            long userCount = userRepository.count(); // Количество пользователей
            long activeObjectsCount = energyObjectService.getAllEnergyObjects()
                    .stream()
                    .filter(EnergyObject::getActive)
                    .count(); // Количество активных объектов

            // 4. Получаем статистику по типам объектов
            Map<String, Long> typeStatistics = energyObjectService.getAllEnergyObjects()
                    .stream()
                    .collect(Collectors.groupingBy(
                            EnergyObject::getType,
                            Collectors.counting()
                    ));

            // 5. Добавляем все атрибуты в модель
            model.addAttribute("energyObjects", energyObjectPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", energyObjectPage.getTotalPages());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("direction", direction);

            // Данные для футера
            model.addAttribute("userCount", userCount);
            model.addAttribute("activeObjectsCount", activeObjectsCount);
            model.addAttribute("typeStatistics", typeStatistics);

            // 6. Генерируем номера страниц для пагинации
            int totalPages = energyObjectPage.getTotalPages();
            if (totalPages > 0) {
                List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                        .boxed()
                        .collect(Collectors.toList());
                model.addAttribute("pageNumbers", pageNumbers);
            }

            // 7. Рассчитываем общую мощность всех активных объектов
            Double totalActivePower = energyObjectService.getAllEnergyObjects()
                    .stream()
                    .filter(EnergyObject::getActive)
                    .mapToDouble(EnergyObject::getPower)
                    .sum();
            model.addAttribute("totalActivePower", totalActivePower != null ? totalActivePower : 0);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при загрузке данных: " + e.getMessage());
            e.printStackTrace(); // Для отладки
        }

        return "energy-objects/list";
    }

    // Остальные методы остаются без изменений...
    @GetMapping("/create")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("energyObject", new EnergyObject());
        return "energy-objects/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public String createEnergyObject(@Valid @ModelAttribute EnergyObject energyObject,
                                     BindingResult result,
                                     Model model) {
        if (result.hasErrors()) {
            return "energy-objects/create";
        }

        try {
            energyObjectService.createEnergyObject(energyObject);
            return "redirect:/energy-objects?success=Объект успешно создан";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при создании объекта: " + e.getMessage());
            return "energy-objects/create";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            EnergyObject energyObject = energyObjectService.getEnergyObjectById(id);
            model.addAttribute("energyObject", energyObject);
            return "energy-objects/edit";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/energy-objects";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public String updateEnergyObject(@PathVariable Long id,
                                     @Valid @ModelAttribute EnergyObject energyObject,
                                     BindingResult result,
                                     Model model) {
        if (result.hasErrors()) {
            return "energy-objects/edit";
        }

        try {
            energyObjectService.updateEnergyObject(id, energyObject);
            return "redirect:/energy-objects?success=Объект успешно обновлен";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка при обновлении объекта: " + e.getMessage());
            return "energy-objects/edit";
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
        List<EnergyObject> allObjects = getAllEnergyObjects();
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

        return Map.of(
                "totalObjects", totalObjects,
                "activeObjects", activeObjects,
                "inactiveObjects", inactiveObjects,
                "totalPower", Math.round(totalPower * 100.0) / 100.0,
                "averagePower", Math.round(averagePower * 100.0) / 100.0,
                "averageEfficiency", Math.round(averageEfficiency * 10.0) / 10.0,
                "typeDistribution", typeDistribution,
                "typePercentages", typePercentages,
                "powerByType", powerByType,
                "efficiencyByType", efficiencyByType,
                "oldestObject", oldestObject,
                "newestObject", newestObject,
                "activePercentage", Math.round((activeObjects * 100.0 / totalObjects) * 10.0) / 10.0
        );
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