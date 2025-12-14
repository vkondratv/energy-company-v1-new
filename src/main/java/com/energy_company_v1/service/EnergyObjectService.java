package com.energy_company_v1.service;

import com.energy_company_v1.model.EnergyObject;
import com.energy_company_v1.repository.EnergyObjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        // Убедитесь, что объект сохраняется с корректными значениями
        if (energyObject.getActive() == null) {
            energyObject.setActive(true); // По умолчанию активен
        }
        return energyObjectRepository.save(energyObject);
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

    public List<EnergyObject> searchEnergyObjects(String keyword, PageRequest pageRequest) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return energyObjectRepository.findAll();
        }
        return energyObjectRepository.searchByKeyword(keyword);
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