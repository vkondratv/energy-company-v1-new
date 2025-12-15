package com.energy_company_v1.service;

import com.energy_company_v1.model.EnergyObject;
import com.energy_company_v1.repository.EnergyObjectRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class EnergyObjectService {
    private final EnergyObjectRepository energyObjectRepository;

    public EnergyObjectService(EnergyObjectRepository energyObjectRepository) {
        this.energyObjectRepository = energyObjectRepository;
    }

    public List<EnergyObject> getAllEnergyObjects() {
        return energyObjectRepository.findAll();
    }

    public Page<EnergyObject> getAllEnergyObjects(Pageable pageable) {
        return energyObjectRepository.findAll(pageable);
    }

    public EnergyObject getEnergyObjectById(Long id) {
        return energyObjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Энергообъект не найден"));
    }

    @Transactional
    public EnergyObject createEnergyObject(EnergyObject energyObject) {
        System.out.println("=== СОЗДАНИЕ ЭНЕРГООБЪЕКТА ===");
        System.out.println("Название: " + energyObject.getName());
        System.out.println("Тип: " + energyObject.getType());
        System.out.println("Местоположение: " + energyObject.getLocation());
        System.out.println("Мощность: " + energyObject.getPower());
        System.out.println("Год ввода: " + energyObject.getCommissioningYear());
        System.out.println("КПД: " + energyObject.getEfficiency());
        System.out.println("Активен: " + energyObject.getActive());

        if (energyObject.getActive() == null) {
            energyObject.setActive(true);
            System.out.println("Установлен активен по умолчанию");
        }

        EnergyObject saved = energyObjectRepository.save(energyObject);
        System.out.println("Сохранен объект с ID: " + saved.getId());

        return saved;
    }

    @Transactional
    public EnergyObject updateEnergyObject(Long id, EnergyObject energyObjectDetails) {
        EnergyObject energyObject = getEnergyObjectById(id);

        energyObject.setName(energyObjectDetails.getName());
        energyObject.setType(energyObjectDetails.getType());
        energyObject.setLocation(energyObjectDetails.getLocation());
        energyObject.setPower(energyObjectDetails.getPower());
        energyObject.setCommissioningYear(energyObjectDetails.getCommissioningYear());
        energyObject.setEfficiency(energyObjectDetails.getEfficiency());
        energyObject.setActive(energyObjectDetails.getActive());
        energyObject.setLastMaintenanceDate(energyObjectDetails.getLastMaintenanceDate());
        energyObject.setDescription(energyObjectDetails.getDescription());

        return energyObjectRepository.save(energyObject);
    }

    @Transactional
    public void deleteEnergyObject(Long id) {
        EnergyObject energyObject = getEnergyObjectById(id);
        energyObjectRepository.delete(energyObject);
    }

    public Page<EnergyObject> searchEnergyObjects(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return energyObjectRepository.searchByKeywordPageable(keyword.toLowerCase(), pageable);
        }

        // Получаем все результаты поиска
        List<EnergyObject> searchResults = energyObjectRepository.searchByKeyword(keyword);

        // Рассчитываем пагинацию вручную
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;

        List<EnergyObject> pageContent;

        if (searchResults.size() < startItem) {
            pageContent = List.of();
        } else {
            int toIndex = Math.min(startItem + pageSize, searchResults.size());
            pageContent = searchResults.subList(startItem, toIndex);
        }

        return new PageImpl<>(
                pageContent,
                pageable,
                searchResults.size()
        );
    }

    public List<EnergyObject> getEnergyObjectsSorted(String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortBy);
        return energyObjectRepository.findAll(sort);
    }

    // Статистические методы
    public Map<String, Object> getStatistics() {
        Double averagePower = energyObjectRepository.findAveragePower();
        Double totalActivePower = energyObjectRepository.findTotalActivePower();
        List<Object[]> countByType = energyObjectRepository.countByType();
        Long totalObjects = energyObjectRepository.count();
        Long activeObjects = energyObjectRepository.findAll().stream()
                .filter(EnergyObject::getActive)
                .count();

        return Map.of(
                "averagePower", averagePower != null ? averagePower : 0,
                "totalActivePower", totalActivePower != null ? totalActivePower : 0,
                "countByType", countByType,
                "totalObjects", totalObjects,
                "activeObjects", activeObjects
        );
    }

    public List<EnergyObject> getAllEnergyObjectsByFilters(Map<String, String> filters) {
        // Если нет фильтров, возвращаем все объекты
        if (filters == null || filters.isEmpty()) {
            return energyObjectRepository.findAll();
        }

        // Здесь можно реализовать логику фильтрации
        // Пока возвращаем все объекты
        return energyObjectRepository.findAll();
    }
}