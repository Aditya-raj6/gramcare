package com.gramcare.resident.repository;

import com.gramcare.resident.entity.CentreService;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CentreServiceRepository extends JpaRepository<CentreService, Long> {
    List<CentreService> findByHealthCentreId(Long healthCentreId);
}
