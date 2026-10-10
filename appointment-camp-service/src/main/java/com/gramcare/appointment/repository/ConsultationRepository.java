package com.gramcare.appointment.repository;

import com.gramcare.appointment.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    Optional<Consultation> findByAppointmentId(Long appointmentId);
    List<Consultation> findAllByAppointmentId(Long appointmentId);
}
