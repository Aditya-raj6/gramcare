package com.gramcare.appointment.repository;

import com.gramcare.appointment.entity.HealthCamp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HealthCampRepository extends JpaRepository<HealthCamp, Long> {
    List<HealthCamp> findByStatus(HealthCamp.CampStatus status);
    List<HealthCamp> findByVillageIgnoreCase(String village);
}
