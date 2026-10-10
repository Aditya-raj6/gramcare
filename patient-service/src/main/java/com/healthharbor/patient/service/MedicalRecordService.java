package com.healthharbor.patient.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.patient.entity.MedicalRecord;
import com.healthharbor.patient.repository.MedicalRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class MedicalRecordService extends CrudService<MedicalRecord> {

    private final ResidentNameResolver residents;

    public MedicalRecordService(MedicalRecordRepository repository, ResidentNameResolver residents) {
        super(repository, MedicalRecord.class, "Medical record");
        this.residents = residents;
    }

    @Override
    protected void beforeSave(MedicalRecord entity, MedicalRecord previous) {
        entity.setResidentName(residents.resolve(entity.getResidentId()));
        if (entity.getRecordDate() == null) {
            entity.setRecordDate(LocalDate.now());
        }
    }
}
