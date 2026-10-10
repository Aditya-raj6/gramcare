package com.gramcare.inventory.repository;

import com.gramcare.inventory.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByNameContainingIgnoreCase(String name);
    Optional<Medicine> findByNameIgnoreCase(String name);
}
