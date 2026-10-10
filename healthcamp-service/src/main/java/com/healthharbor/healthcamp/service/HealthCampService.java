package com.healthharbor.healthcamp.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.common.dto.StatusUpdateRequest;
import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.healthcamp.entity.HealthCamp;
import com.healthharbor.healthcamp.entity.HealthCamp.CampStatus;
import com.healthharbor.healthcamp.repository.HealthCampRepository;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class HealthCampService extends CrudService<HealthCamp> {

    private static final Map<CampStatus, Set<CampStatus>> TRANSITIONS = Map.of(
            CampStatus.PLANNED, EnumSet.of(CampStatus.ONGOING, CampStatus.CANCELLED),
            CampStatus.ONGOING, EnumSet.of(CampStatus.COMPLETED, CampStatus.CANCELLED),
            CampStatus.COMPLETED, EnumSet.noneOf(CampStatus.class),
            CampStatus.CANCELLED, EnumSet.of(CampStatus.PLANNED));

    public HealthCampService(HealthCampRepository repository) {
        super(repository, HealthCamp.class, "Health camp");
    }

    @Override
    protected void beforeSave(HealthCamp camp, HealthCamp previous) {
        if (camp.getEndDate() == null) {
            camp.setEndDate(camp.getStartDate());
        }
        if (camp.getEndDate().isBefore(camp.getStartDate())) {
            throw new BusinessException("Camp end date cannot be before the start date");
        }
    }

    @Override
    protected void afterCreate(HealthCamp camp) {
        camp.setCampCode(reference("CAMP", camp.getId()));
        repository.save(camp);
    }

    @Override
    protected void validateTransition(HealthCamp camp, Object oldStatus, Object newStatus) {
        if (oldStatus != newStatus && !TRANSITIONS.get((CampStatus) oldStatus).contains((CampStatus) newStatus)) {
            throw new BusinessException("Cannot move camp from " + oldStatus + " to " + newStatus);
        }
    }

    @Override
    protected void onStatusChange(HealthCamp camp, Object oldStatus, Object newStatus, StatusUpdateRequest request) {
        if (newStatus == CampStatus.COMPLETED && request.notes() != null) {
            camp.setOutcomeSummary(request.notes());
        }
    }
}
