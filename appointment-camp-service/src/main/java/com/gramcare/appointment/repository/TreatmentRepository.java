package com.gramcare.appointment.repository;

import com.gramcare.appointment.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    List<Treatment> findByConsultationId(Long consultationId);
}
