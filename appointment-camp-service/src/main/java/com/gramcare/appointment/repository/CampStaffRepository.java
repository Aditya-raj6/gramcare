package com.gramcare.appointment.repository;

import com.gramcare.appointment.entity.CampStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CampStaffRepository extends JpaRepository<CampStaff, Long> {
    List<CampStaff> findByHealthCampId(Long healthCampId);
}
