package com.energy_company_v1.repository;

import com.energy_company_v1.model.EnergyObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnergyObjectRepository extends JpaRepository<EnergyObject, Long> {
    List<EnergyObject> findByNameContainingIgnoreCase(String name);
    List<EnergyObject> findByLocationContainingIgnoreCase(String location);
    List<EnergyObject> findByType(String type);
    List<EnergyObject> findByActiveTrue();

    @Query("SELECT e FROM EnergyObject e WHERE " +
            "LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.type) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<EnergyObject> searchByKeyword(@Param("keyword") String keyword);

    Page<EnergyObject> findAll(Pageable pageable);

    // Статистические запросы
    @Query("SELECT AVG(e.power) FROM EnergyObject e")
    Double findAveragePower();

    @Query("SELECT SUM(e.power) FROM EnergyObject e WHERE e.active = true")
    Double findTotalActivePower();

    @Query("SELECT e.type, COUNT(e) FROM EnergyObject e GROUP BY e.type")
    List<Object[]> countByType();

    @Query("SELECT e FROM EnergyObject e WHERE " +
            "LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.type) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<EnergyObject> searchByKeywordPageable(@Param("keyword") String keyword, Pageable pageable);

    // Или более простой вариант:
    Page<EnergyObject> findByNameContainingIgnoreCaseOrLocationContainingIgnoreCaseOrTypeContainingIgnoreCase(
            String name, String location, String type, Pageable pageable);
}