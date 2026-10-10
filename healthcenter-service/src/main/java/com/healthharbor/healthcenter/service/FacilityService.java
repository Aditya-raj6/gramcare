package com.healthharbor.healthcenter.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.healthcenter.entity.Facility;
import com.healthharbor.healthcenter.repository.FacilityRepository;
import org.springframework.stereotype.Service;

@Service
public class FacilityService extends CrudService<Facility> {

    private final CentreResolver resolver;

    public FacilityService(FacilityRepository repository, CentreResolver resolver) {
        super(repository, Facility.class, "Facility");
        this.resolver = resolver;
    }

    @Override
    protected void beforeSave(Facility entity, Facility previous) {
        entity.setCentreName(resolver.centreName(entity.getCentreId()));
    }
}
