package com.gramcare.resident.repository;

import com.gramcare.resident.entity.HealthcareWorker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HealthcareWorkerRepository extends JpaRepository<HealthcareWorker, Long> {
    List<HealthcareWorker> findByHealthCentreId(Long healthCentreId);
}
