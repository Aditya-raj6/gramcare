package com.gramcare.resident.repository;

import com.gramcare.resident.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResidentRepository extends JpaRepository<Resident, Long> {
    List<Resident> findByVillageIgnoreCase(String village);
    List<Resident> findByFullNameContainingIgnoreCase(String name);
}
