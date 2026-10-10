package com.gramcare.sanitation.repository;

import com.gramcare.sanitation.entity.CleanlinessInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CleanlinessInspectionRepository extends JpaRepository<CleanlinessInspection, Long> {
    List<CleanlinessInspection> findByFacilityIdOrderByInspectionDateDesc(Long facilityId);
}
