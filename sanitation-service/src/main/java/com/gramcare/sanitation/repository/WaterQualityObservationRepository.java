package com.gramcare.sanitation.repository;

import com.gramcare.sanitation.entity.WaterQualityObservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WaterQualityObservationRepository extends JpaRepository<WaterQualityObservation, Long> {
    List<WaterQualityObservation> findByVillageIgnoreCase(String village);
    List<WaterQualityObservation> findByReviewStatus(WaterQualityObservation.ReviewStatus status);
    List<WaterQualityObservation> findByReviewStatusAndActionItemIdIsNull(WaterQualityObservation.ReviewStatus status);
}
