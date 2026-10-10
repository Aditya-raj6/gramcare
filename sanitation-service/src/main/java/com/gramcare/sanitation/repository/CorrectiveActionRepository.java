package com.gramcare.sanitation.repository;

import com.gramcare.sanitation.entity.CorrectiveAction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CorrectiveActionRepository extends JpaRepository<CorrectiveAction, Long> {
    List<CorrectiveAction> findBySanitationComplaintId(Long complaintId);
    List<CorrectiveAction> findByWaterQualityObservationId(Long observationId);
}
