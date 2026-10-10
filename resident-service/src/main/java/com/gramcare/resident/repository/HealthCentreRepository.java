package com.gramcare.resident.repository;

import com.gramcare.resident.entity.HealthCentre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HealthCentreRepository extends JpaRepository<HealthCentre, Long> {
    List<HealthCentre> findByVillageIgnoreCase(String village);
    List<HealthCentre> findByNameContainingIgnoreCase(String name);
    List<HealthCentre> findByActiveTrue();
}
