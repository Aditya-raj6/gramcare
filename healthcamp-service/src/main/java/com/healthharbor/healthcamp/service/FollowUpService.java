package com.healthharbor.healthcamp.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.common.dto.StatusUpdateRequest;
import com.healthharbor.healthcamp.entity.Beneficiary;
import com.healthharbor.healthcamp.entity.FollowUp;
import com.healthharbor.healthcamp.repository.FollowUpRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class FollowUpService extends CrudService<FollowUp> {
    private final CampResolver resolver;

    public FollowUpService(FollowUpRepository repository, CampResolver resolver) {
        super(repository, FollowUp.class, "Follow-up");
        this.resolver = resolver;
    }

    @Override
    protected void beforeSave(FollowUp f, FollowUp previous) {
        Beneficiary b = resolver.beneficiary(f.getBeneficiaryId());
        f.setBeneficiaryName(b.getName());
        f.setVillage(b.getVillage());
        f.setCampId(b.getCampId());
        f.setCampName(b.getCampName());
        if (f.getStatus() == FollowUp.FollowUpStatus.COMPLETED && f.getCompletedDate() == null) {
            f.setCompletedDate(LocalDate.now());
        }
    }

    @Override
    protected void onStatusChange(FollowUp f, Object oldStatus, Object newStatus, StatusUpdateRequest request) {
        if (newStatus == FollowUp.FollowUpStatus.COMPLETED) {
            f.setCompletedDate(LocalDate.now());
        }
        if (request.notes() != null && !request.notes().isBlank()) {
            f.setOutcomeNotes(request.notes());
        }
    }
}
