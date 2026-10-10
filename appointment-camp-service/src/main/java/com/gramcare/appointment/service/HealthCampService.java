package com.gramcare.appointment.service;

import com.gramcare.appointment.dto.HealthCampRequest;
import com.gramcare.appointment.entity.*;
import com.gramcare.appointment.exception.ResourceNotFoundException;
import com.gramcare.appointment.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HealthCampService {

    private final HealthCampRepository healthCampRepository;
    private final CampStaffRepository campStaffRepository;
    private final CampBeneficiaryRepository campBeneficiaryRepository;
    private final ScreeningRecordRepository screeningRecordRepository;
    private final FollowUpRepository followUpRepository;

    public HealthCampService(HealthCampRepository healthCampRepository,
                              CampStaffRepository campStaffRepository,
                              CampBeneficiaryRepository campBeneficiaryRepository,
                              ScreeningRecordRepository screeningRecordRepository,
                              FollowUpRepository followUpRepository) {
        this.healthCampRepository = healthCampRepository;
        this.campStaffRepository = campStaffRepository;
        this.campBeneficiaryRepository = campBeneficiaryRepository;
        this.screeningRecordRepository = screeningRecordRepository;
        this.followUpRepository = followUpRepository;
    }

    @Transactional(readOnly = true)
    public List<HealthCamp> findAll() { return healthCampRepository.findAll(); }

    @Transactional(readOnly = true)
    public HealthCamp findById(Long id) {
        return healthCampRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health camp not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<HealthCamp> findActive() {
        return healthCampRepository.findAll().stream()
                .filter(c -> c.getStatus() == HealthCamp.CampStatus.PLANNED || c.getStatus() == HealthCamp.CampStatus.ONGOING)
                .toList();
    }

    public HealthCamp create(HealthCampRequest req) {
        HealthCamp c = new HealthCamp();
        c.setCampName(req.getCampName());
        c.setVillage(req.getVillage());
        c.setCampDate(req.getCampDate());
        c.setLocation(req.getLocation());
        return healthCampRepository.save(c);
    }

    public HealthCamp updateStatus(Long id, HealthCamp.CampStatus status) {
        HealthCamp c = findById(id);
        c.setStatus(status);
        return healthCampRepository.save(c);
    }

    public CampStaff assignStaff(Long campId, CampStaff staff) {
        HealthCamp camp = findById(campId);
        staff.setHealthCamp(camp);
        return campStaffRepository.save(staff);
    }

    @Transactional(readOnly = true)
    public List<CampStaff> getStaff(Long campId) { return campStaffRepository.findByHealthCampId(campId); }

    public CampBeneficiary registerBeneficiary(Long campId, CampBeneficiary beneficiary) {
        HealthCamp camp = findById(campId);
        beneficiary.setHealthCamp(camp);
        return campBeneficiaryRepository.save(beneficiary);
    }

    @Transactional(readOnly = true)
    public List<CampBeneficiary> getBeneficiaries(Long campId) { return campBeneficiaryRepository.findByHealthCampId(campId); }

    public ScreeningRecord addScreening(Long beneficiaryId, ScreeningRecord screening) {
        CampBeneficiary beneficiary = campBeneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found with id " + beneficiaryId));
        screening.setBeneficiary(beneficiary);
        return screeningRecordRepository.save(screening);
    }

    @Transactional(readOnly = true)
    public List<ScreeningRecord> getScreenings(Long beneficiaryId) { return screeningRecordRepository.findByBeneficiaryId(beneficiaryId); }

    public FollowUp addFollowUp(Long beneficiaryId, FollowUp followUp) {
        CampBeneficiary beneficiary = campBeneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found with id " + beneficiaryId));
        followUp.setBeneficiary(beneficiary);
        return followUpRepository.save(followUp);
    }

    @Transactional(readOnly = true)
    public List<FollowUp> getFollowUps(Long beneficiaryId) { return followUpRepository.findByBeneficiaryId(beneficiaryId); }

    @Transactional(readOnly = true)
    public List<FollowUp> getPendingFollowUps() { return followUpRepository.findByCompletedFalse(); }
}
