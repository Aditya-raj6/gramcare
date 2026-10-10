package com.healthharbor.healthcenter.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.healthcenter.entity.ServiceOffering;
import com.healthharbor.healthcenter.repository.ServiceOfferingRepository;
import org.springframework.stereotype.Service;

@Service
public class ServiceOfferingService extends CrudService<ServiceOffering> {

    private final CentreResolver resolver;

    public ServiceOfferingService(ServiceOfferingRepository repository, CentreResolver resolver) {
        super(repository, ServiceOffering.class, "Centre Service");
        this.resolver = resolver;
    }

    @Override
    protected void beforeSave(ServiceOffering entity, ServiceOffering previous) {
        entity.setCentreName(resolver.centreName(entity.getCentreId()));
    }
}
