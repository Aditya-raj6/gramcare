package com.healthharbor.sanitation.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.sanitation.entity.SanitationFacility;
import com.healthharbor.sanitation.repository.SanitationFacilityRepository;
import org.springframework.stereotype.Service;

@Service
public class SanitationFacilityService extends CrudService<SanitationFacility> {

    public SanitationFacilityService(SanitationFacilityRepository repository) {
        super(repository, SanitationFacility.class, "Sanitation facility");
    }

    public SanitationFacility require(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException("Sanitation facility " + id + " does not exist"));
    }

    void save(SanitationFacility facility) {
        repository.save(facility);
    }

    @Override
    protected void afterCreate(SanitationFacility entity) {
        if (entity.getFacilityCode() == null || entity.getFacilityCode().isBlank()) {
            entity.setFacilityCode(reference("SAN", entity.getId()));
            repository.save(entity);
        }
    }
}
