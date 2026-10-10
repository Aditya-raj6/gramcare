package com.gramcare.appointment.repository;

import com.gramcare.appointment.entity.FollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {
    List<FollowUp> findByBeneficiaryId(Long beneficiaryId);
    List<FollowUp> findByCompletedFalse();
}
