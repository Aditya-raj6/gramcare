package com.gramcare.resident.repository;

import com.gramcare.resident.entity.OperatingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OperatingScheduleRepository extends JpaRepository<OperatingSchedule, Long> {
    List<OperatingSchedule> findByHealthCentreId(Long healthCentreId);
}
