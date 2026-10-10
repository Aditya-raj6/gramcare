package com.healthharbor.patient.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.patient.entity.Visit;
import com.healthharbor.patient.repository.VisitRepository;
import org.springframework.stereotype.Service;

@Service
public class VisitService extends CrudService<Visit> {

    private final ResidentNameResolver residents;

    public VisitService(VisitRepository repository, ResidentNameResolver residents) {
        super(repository, Visit.class, "Visit");
        this.residents = residents;
    }

    @Override
    protected void beforeSave(Visit entity, Visit previous) {
        entity.setResidentName(residents.resolve(entity.getResidentId()));
    }
}
