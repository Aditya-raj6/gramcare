package com.gramcare.appointment.repository;

import com.gramcare.appointment.entity.CampBeneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CampBeneficiaryRepository extends JpaRepository<CampBeneficiary, Long> {
    List<CampBeneficiary> findByHealthCampId(Long healthCampId);
}
