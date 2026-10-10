package com.gramcare.appointment.repository;

import com.gramcare.appointment.entity.ScreeningRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScreeningRecordRepository extends JpaRepository<ScreeningRecord, Long> {
    List<ScreeningRecord> findByBeneficiaryId(Long beneficiaryId);
}
