package com.gramcare.resident.service;

import com.gramcare.resident.dto.MedicalRecordRequest;
import com.gramcare.resident.dto.ResidentRequest;
import com.gramcare.resident.entity.MedicalRecord;
import com.gramcare.resident.entity.Resident;
import com.gramcare.resident.exception.ResourceNotFoundException;
import com.gramcare.resident.repository.MedicalRecordRepository;
import com.gramcare.resident.repository.ResidentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public ResidentService(ResidentRepository residentRepository, MedicalRecordRepository medicalRecordRepository) {
        this.residentRepository = residentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    @Transactional(readOnly = true)
    public List<Resident> findAll() {
        return residentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Resident findById(Long id) {
        return residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<Resident> search(String name, String village) {
        if (name != null && !name.isBlank()) {
            return residentRepository.findByFullNameContainingIgnoreCase(name);
        }
        if (village != null && !village.isBlank()) {
            return residentRepository.findByVillageIgnoreCase(village);
        }
        return residentRepository.findAll();
    }

    public Resident create(ResidentRequest req) {
        Resident r = new Resident();
        apply(r, req);
        return residentRepository.save(r);
    }

    public Resident update(Long id, ResidentRequest req) {
        Resident r = findById(id);
        apply(r, req);
        return residentRepository.save(r);
    }

    private void apply(Resident r, ResidentRequest req) {
        r.setFullName(req.getFullName());
        r.setDateOfBirth(req.getDateOfBirth());
        r.setGender(req.getGender());
        r.setVillage(req.getVillage());
        r.setHouseholdAddress(req.getHouseholdAddress());
        r.setContactPhone(req.getContactPhone());
        r.setBloodGroup(req.getBloodGroup());
        r.setKnownAllergies(req.getKnownAllergies());
        r.setChronicConditions(req.getChronicConditions());
    }

    public void delete(Long id) {
        Resident r = findById(id);
        residentRepository.delete(r);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getVisitHistory(Long residentId) {
        findById(residentId);
        return medicalRecordRepository.findByResidentIdOrderByVisitDateDesc(residentId);
    }

    public MedicalRecord addVisit(Long residentId, MedicalRecordRequest req) {
        Resident resident = findById(residentId);
        MedicalRecord mr = new MedicalRecord();
        mr.setResident(resident);
        mr.setVisitDate(req.getVisitDate());
        mr.setReasonForVisit(req.getReasonForVisit());
        mr.setDiagnosisSummary(req.getDiagnosisSummary());
        mr.setNotes(req.getNotes());
        mr.setHealthCentreId(req.getHealthCentreId());
        return medicalRecordRepository.save(mr);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return residentRepository.count();
    }
}
