package com.gramcare.inventory.repository;

import com.gramcare.inventory.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {
    List<InventoryBatch> findByMedicineIdOrderByExpiryDateAsc(Long medicineId);
    List<InventoryBatch> findByExpiryDateBefore(LocalDate date);
}
