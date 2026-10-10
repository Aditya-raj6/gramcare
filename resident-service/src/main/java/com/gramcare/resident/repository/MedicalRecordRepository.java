package com.gramcare.resident.repository;

import com.gramcare.resident.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByResidentIdOrderByVisitDateDesc(Long residentId);
}
