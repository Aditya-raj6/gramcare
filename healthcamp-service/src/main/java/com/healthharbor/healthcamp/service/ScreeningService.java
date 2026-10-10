package com.healthharbor.healthcamp.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.healthcamp.entity.Beneficiary;
import com.healthharbor.healthcamp.entity.FollowUp;
import com.healthharbor.healthcamp.entity.Screening;
import com.healthharbor.healthcamp.repository.FollowUpRepository;
import com.healthharbor.healthcamp.repository.ScreeningRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/** Abnormal or critical screenings that are referred automatically generate a follow-up task. */
@Service
public class ScreeningService extends CrudService<Screening> {
    private final CampResolver resolver;
    private final FollowUpRepository followUps;

    public ScreeningService(ScreeningRepository repository, CampResolver resolver, FollowUpRepository followUps) {
        super(repository, Screening.class, "Screening");
        this.resolver = resolver;
        this.followUps = followUps;
    }

    @Override
    protected void beforeSave(Screening s, Screening previous) {
        Beneficiary b = resolver.beneficiary(s.getBeneficiaryId());
        s.setBeneficiaryName(b.getName());
        s.setCampId(b.getCampId());
        s.setCampName(b.getCampName());
        if (s.getScreeningDate() == null) {
            s.setScreeningDate(LocalDate.now());
        }
        if (s.getOutcome() == Screening.Outcome.CRITICAL) {
            s.setReferred(true);
        }
    }

    @Override
    protected void afterCreate(Screening s) {
        if (Boolean.TRUE.equals(s.getReferred()) && s.getOutcome() != Screening.Outcome.NORMAL) {
            Beneficiary b = resolver.beneficiary(s.getBeneficiaryId());
            boolean critical = s.getOutcome() == Screening.Outcome.CRITICAL;
            FollowUp f = new FollowUp();
            f.setCampId(s.getCampId());
            f.setCampName(s.getCampName());
            f.setBeneficiaryId(b.getId());
            f.setBeneficiaryName(b.getName());
            f.setVillage(b.getVillage());
            f.setScreeningId(s.getId());
            f.setAction("Review " + s.getScreeningType().name().replace('_', ' ').toLowerCase() + " result ("
                    + s.getResultValue() + ")" + (s.getReferredTo() == null ? "" : " at " + s.getReferredTo()));
            f.setDueDate(LocalDate.now().plusDays(critical ? 2 : 7));
            f.setPriority(critical ? FollowUp.Priority.CRITICAL : FollowUp.Priority.HIGH);
            f.setAssignedTo(s.getScreenedBy());
            followUps.save(f);
        }
    }
}
