package com.healthharbor.appointment.repository;

import com.healthharbor.appointment.entity.Consultation;
import com.healthharbor.common.crud.CrudRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConsultationRepository extends CrudRepository<Consultation> {

    Optional<Consultation> findByAppointmentId(Long appointmentId);

    @Query("select c.diagnosis, count(c) from Consultation c group by c.diagnosis order by count(c) desc")
    List<Object[]> topDiagnoses(Pageable pageable);
}
