package com.healthharbor.healthcamp.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.healthcamp.entity.CampStaff;
import com.healthharbor.healthcamp.repository.CampStaffRepository;
import org.springframework.stereotype.Service;

@Service
public class CampStaffService extends CrudService<CampStaff> {
    private final CampResolver resolver;

    public CampStaffService(CampStaffRepository repository, CampResolver resolver) {
        super(repository, CampStaff.class, "Camp staff");
        this.resolver = resolver;
    }

    @Override
    protected void beforeSave(CampStaff entity, CampStaff previous) {
        entity.setCampName(resolver.camp(entity.getCampId()).getName());
    }
}
