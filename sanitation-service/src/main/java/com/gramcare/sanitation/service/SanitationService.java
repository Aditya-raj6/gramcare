package com.gramcare.sanitation.service;

import com.gramcare.sanitation.dto.*;
import com.gramcare.sanitation.entity.*;
import com.gramcare.sanitation.exception.ResourceNotFoundException;
import com.gramcare.sanitation.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SanitationService {

    private final SanitationFacilityRepository facilityRepository;
    private final CleanlinessInspectionRepository inspectionRepository;
    private final SanitationComplaintRepository complaintRepository;
    private final WaterQualityObservationRepository waterRepository;
    private final CorrectiveActionRepository correctiveActionRepository;
    private final ActionServiceClient actionServiceClient;

    public SanitationService(SanitationFacilityRepository facilityRepository,
                              CleanlinessInspectionRepository inspectionRepository,
                              SanitationComplaintRepository complaintRepository,
                              WaterQualityObservationRepository waterRepository,
                              CorrectiveActionRepository correctiveActionRepository,
                              ActionServiceClient actionServiceClient) {
        this.facilityRepository = facilityRepository;
        this.inspectionRepository = inspectionRepository;
        this.complaintRepository = complaintRepository;
        this.waterRepository = waterRepository;
        this.correctiveActionRepository = correctiveActionRepository;
        this.actionServiceClient = actionServiceClient;
    }

    // ---- Facilities ----

    @Transactional(readOnly = true)
    public List<SanitationFacility> findAllFacilities() { return facilityRepository.findAll(); }

    @Transactional(readOnly = true)
    public SanitationFacility findFacility(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sanitation facility not found with id " + id));
    }

    public SanitationFacility createFacility(FacilityRequest req) {
        SanitationFacility f = new SanitationFacility();
        f.setFacilityName(req.getFacilityName());
        if (req.getFacilityType() != null) f.setFacilityType(req.getFacilityType());
        f.setVillage(req.getVillage());
        f.setLocation(req.getLocation());
        return facilityRepository.save(f);
    }

    public CleanlinessInspection addInspection(Long facilityId, CleanlinessInspection inspection) {
        SanitationFacility facility = findFacility(facilityId);
        inspection.setFacility(facility);
        facility.setCleanlinessStatus(inspection.getObservedStatus());
        facilityRepository.save(facility);
        return inspectionRepository.save(inspection);
    }

    @Transactional(readOnly = true)
    public List<CleanlinessInspection> getInspections(Long facilityId) {
        return inspectionRepository.findByFacilityIdOrderByInspectionDateDesc(facilityId);
    }

    // ---- Complaints ----

    @Transactional(readOnly = true)
    public List<SanitationComplaint> findComplaints(String status) {
        if (status == null || status.isBlank()) return complaintRepository.findAll();
        return complaintRepository.findByStatus(SanitationComplaint.ComplaintStatus.valueOf(status.toUpperCase()));
    }

    @Transactional(readOnly = true)
    public SanitationComplaint findComplaint(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id " + id));
    }

    /**
     * Saves the complaint first (its own record is the source of truth), then
     * tries to create a linked action item in the Action Tracking Service.
     * If that downstream call fails, the complaint is still returned, with
     * actionItemId left null so the caller/UI can see linkage did not happen
     * and retry via {@link #retryActionLink(Long)}.
     */
    public SanitationComplaint reportComplaint(ComplaintRequest req) {
        SanitationComplaint complaint = new SanitationComplaint();
        if (req.getFacilityId() != null) {
            complaint.setFacility(findFacility(req.getFacilityId()));
        }
        complaint.setTitle(req.getTitle());
        complaint.setDescription(req.getDescription());
        complaint.setReportedBy(req.getReportedBy());
        if (req.getPriority() != null) complaint.setPriority(req.getPriority());
        SanitationComplaint saved = complaintRepository.save(complaint);

        linkComplaintToActionService(saved);
        return saved;
    }

    public SanitationComplaint retryActionLink(Long complaintId) {
        SanitationComplaint complaint = findComplaint(complaintId);
        linkComplaintToActionService(complaint);
        return complaint;
    }

    private void linkComplaintToActionService(SanitationComplaint complaint) {
        if (complaint.getActionItemId() != null) return;
        ActionItemRequest req = new ActionItemRequest();
        req.setTitle(complaint.getTitle());
        req.setDescription(complaint.getDescription());
        req.setCategory("SANITATION");
        req.setVillage(complaint.getFacility() != null ? complaint.getFacility().getVillage() : null);
        req.setPriority(complaint.getPriority().name());
        req.setReportedBy(complaint.getReportedBy());
        req.setSourceService("sanitation-service");
        req.setSourceReferenceType("SANITATION_COMPLAINT");
        req.setSourceReferenceId(complaint.getId());

        Optional<ActionItemResponse> response = actionServiceClient.createActionItem(req);
        response.ifPresent(r -> {
            complaint.setActionItemId(r.getId());
            complaintRepository.save(complaint);
        });
    }

    public SanitationComplaint updateComplaintStatus(Long id, String status, String assignedWorker, String closureNotes) {
        SanitationComplaint complaint = findComplaint(id);
        if (status != null) complaint.setStatus(SanitationComplaint.ComplaintStatus.valueOf(status.toUpperCase()));
        if (assignedWorker != null) complaint.setAssignedWorker(assignedWorker);
        if (closureNotes != null) complaint.setClosureNotes(closureNotes);
        if (complaint.getStatus() == SanitationComplaint.ComplaintStatus.RESOLVED
                || complaint.getStatus() == SanitationComplaint.ComplaintStatus.CLOSED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }
        return complaintRepository.save(complaint);
    }

    // ---- Water quality ----

    @Transactional(readOnly = true)
    public List<WaterQualityObservation> findObservations(String village) {
        if (village == null || village.isBlank()) return waterRepository.findAll();
        return waterRepository.findByVillageIgnoreCase(village);
    }

    @Transactional(readOnly = true)
    public WaterQualityObservation findObservation(Long id) {
        return waterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Water quality observation not found with id " + id));
    }

    public WaterQualityObservation recordObservation(WaterObservationRequest req) {
        WaterQualityObservation obs = new WaterQualityObservation();
        obs.setVillage(req.getVillage());
        obs.setWaterSourceName(req.getWaterSourceName());
        obs.setObservationDate(req.getObservationDate());
        obs.setPh(req.getPh());
        obs.setTurbidityNtu(req.getTurbidityNtu());
        obs.setResidualChlorineMgL(req.getResidualChlorineMgL());
        obs.setRecordedBy(req.getRecordedBy());
        obs.setNotes(req.getNotes());

        if (obs.isOutOfRange()) {
            obs.setReviewStatus(WaterQualityObservation.ReviewStatus.FLAGGED_FOR_REVIEW);
        }
        WaterQualityObservation saved = waterRepository.save(obs);

        if (saved.isOutOfRange()) {
            linkObservationToActionService(saved);
        }
        return saved;
    }

    private void linkObservationToActionService(WaterQualityObservation obs) {
        if (obs.getActionItemId() != null) return;
        ActionItemRequest req = new ActionItemRequest();
        req.setTitle("Water quality out of range: " + obs.getWaterSourceName());
        req.setDescription("Observation on " + obs.getObservationDate() + " recorded pH=" + obs.getPh()
                + ", turbidity=" + obs.getTurbidityNtu() + " NTU, residual chlorine=" + obs.getResidualChlorineMgL()
                + " mg/L, outside the configured reference limits.");
        req.setCategory("WATER_QUALITY");
        req.setVillage(obs.getVillage());
        req.setPriority("HIGH");
        req.setReportedBy(obs.getRecordedBy());
        req.setSourceService("sanitation-service");
        req.setSourceReferenceType("WATER_QUALITY_OBSERVATION");
        req.setSourceReferenceId(obs.getId());

        Optional<ActionItemResponse> response = actionServiceClient.createActionItem(req);
        response.ifPresent(r -> {
            obs.setActionItemId(r.getId());
            waterRepository.save(obs);
        });
    }

    @Transactional(readOnly = true)
    public List<WaterQualityObservation> findObservationsByReviewStatus(String status) {
        return waterRepository.findByReviewStatus(WaterQualityObservation.ReviewStatus.valueOf(status.toUpperCase()));
    }

    public WaterQualityObservation updateReviewStatus(Long id, String status) {
        WaterQualityObservation obs = findObservation(id);
        obs.setReviewStatus(WaterQualityObservation.ReviewStatus.valueOf(status.toUpperCase()));
        return waterRepository.save(obs);
    }

    public CorrectiveAction addCorrectiveAction(CorrectiveAction action) {
        return correctiveActionRepository.save(action);
    }

    @Transactional(readOnly = true)
    public List<CorrectiveAction> getCorrectiveActionsForObservation(Long obsId) {
        return correctiveActionRepository.findByWaterQualityObservationId(obsId);
    }

    @Transactional(readOnly = true)
    public List<CorrectiveAction> getCorrectiveActionsForComplaint(Long complaintId) {
        return correctiveActionRepository.findBySanitationComplaintId(complaintId);
    }

    @Transactional(readOnly = true)
    public long countOpenComplaints() {
        return complaintRepository.findByStatus(SanitationComplaint.ComplaintStatus.OPEN).size();
    }

    @Transactional(readOnly = true)
    public long countFlaggedWaterIssues() {
        return waterRepository.findByReviewStatus(WaterQualityObservation.ReviewStatus.FLAGGED_FOR_REVIEW).size();
    }

    /**
     * Links complaints and flagged water observations that were saved while
     * the Action Tracking Service was unavailable. Returns how many remain
     * unlinked. Safe to repeat because action creation is idempotent.
     */
    public int reconcileUnlinked() {
        int remaining = 0;
        for (SanitationComplaint c : complaintRepository.findByActionItemIdIsNull()) {
            linkComplaintToActionService(c);
            if (c.getActionItemId() == null) remaining++;
        }
        for (WaterQualityObservation o : waterRepository.findByReviewStatusAndActionItemIdIsNull(
                WaterQualityObservation.ReviewStatus.FLAGGED_FOR_REVIEW)) {
            linkObservationToActionService(o);
            if (o.getActionItemId() == null) remaining++;
        }
        return remaining;
    }

    public WaterQualityObservation retryObservationLink(Long id) {
        WaterQualityObservation obs = findObservation(id);
        if (obs.isOutOfRange()) linkObservationToActionService(obs);
        return obs;
    }
}
