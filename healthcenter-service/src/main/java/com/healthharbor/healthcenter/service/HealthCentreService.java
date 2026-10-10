package com.healthharbor.healthcenter.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.healthcenter.entity.HealthCentre;
import com.healthharbor.healthcenter.repository.HealthCentreRepository;
import org.springframework.stereotype.Service;

@Service
public class HealthCentreService extends CrudService<HealthCentre> {

    public HealthCentreService(HealthCentreRepository repository) {
        super(repository, HealthCentre.class, "Health centre");
    }

    @Override
    protected void afterCreate(HealthCentre entity) {
        if (entity.getCode() == null || entity.getCode().isBlank()) {
            entity.setCode(reference("HC", entity.getId()));
            repository.save(entity);
        }
    }
}
