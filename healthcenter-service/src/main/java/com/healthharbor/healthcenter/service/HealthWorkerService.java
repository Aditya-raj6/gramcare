package com.healthharbor.healthcenter.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.healthcenter.entity.HealthWorker;
import com.healthharbor.healthcenter.repository.HealthWorkerRepository;
import org.springframework.stereotype.Service;

@Service
public class HealthWorkerService extends CrudService<HealthWorker> {

    private final CentreResolver resolver;

    public HealthWorkerService(HealthWorkerRepository repository, CentreResolver resolver) {
        super(repository, HealthWorker.class, "Health Worker");
        this.resolver = resolver;
    }

    @Override
    protected void beforeSave(HealthWorker entity, HealthWorker previous) {
        entity.setCentreName(resolver.centreName(entity.getCentreId()));
    }
}
