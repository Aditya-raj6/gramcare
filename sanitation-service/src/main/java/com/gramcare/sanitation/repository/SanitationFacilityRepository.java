package com.gramcare.sanitation.repository;

import com.gramcare.sanitation.entity.SanitationFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SanitationFacilityRepository extends JpaRepository<SanitationFacility, Long> {
    List<SanitationFacility> findByVillageIgnoreCase(String village);
}
