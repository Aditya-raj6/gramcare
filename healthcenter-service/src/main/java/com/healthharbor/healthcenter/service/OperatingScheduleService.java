package com.healthharbor.healthcenter.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.healthcenter.entity.OperatingSchedule;
import com.healthharbor.healthcenter.repository.OperatingScheduleRepository;
import org.springframework.stereotype.Service;

@Service
public class OperatingScheduleService extends CrudService<OperatingSchedule> {

    private final CentreResolver resolver;

    public OperatingScheduleService(OperatingScheduleRepository repository, CentreResolver resolver) {
        super(repository, OperatingSchedule.class, "Operating schedule");
        this.resolver = resolver;
    }

    @Override
    protected void beforeSave(OperatingSchedule entity, OperatingSchedule previous) {
        entity.setCentreName(resolver.centreName(entity.getCentreId()));
        entity.setWorkerName(resolver.workerName(entity.getWorkerId()));
        if (!Boolean.TRUE.equals(entity.getClosed()) && entity.getOpenTime() != null && entity.getCloseTime() != null
                && !entity.getCloseTime().isAfter(entity.getOpenTime())) {
            throw new BusinessException("Closing time must be after opening time");
        }
    }
}
