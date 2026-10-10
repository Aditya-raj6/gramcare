package com.healthharbor.healthcenter.service;

import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.healthcenter.entity.HealthCentre;
import com.healthharbor.healthcenter.entity.HealthWorker;
import com.healthharbor.healthcenter.repository.HealthCentreRepository;
import com.healthharbor.healthcenter.repository.HealthWorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CentreResolver {
    private final HealthCentreRepository centres;
    private final HealthWorkerRepository workers;

    public String centreName(Long centreId) {
        return centres.findById(centreId).map(HealthCentre::getName)
                .orElseThrow(() -> new BusinessException("Health centre " + centreId + " does not exist"));
    }

    public String workerName(Long workerId) {
        return workerId == null ? null : workers.findById(workerId).map(HealthWorker::getName)
                .orElseThrow(() -> new BusinessException("Health worker " + workerId + " does not exist"));
    }
}
