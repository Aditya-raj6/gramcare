package com.healthharbor.healthcamp.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.healthcamp.entity.Beneficiary;
import com.healthharbor.healthcamp.entity.HealthCamp;
import com.healthharbor.healthcamp.repository.BeneficiaryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BeneficiaryService extends CrudService<Beneficiary> {
    private final CampResolver resolver;

    public BeneficiaryService(BeneficiaryRepository repository, CampResolver resolver) {
        super(repository, Beneficiary.class, "Beneficiary");
        this.resolver = resolver;
    }

    @Override
    protected void beforeSave(Beneficiary entity, Beneficiary previous) {
        HealthCamp camp = resolver.camp(entity.getCampId());
        if (previous == null && (camp.getStatus() == HealthCamp.CampStatus.CANCELLED || camp.getStatus() == HealthCamp.CampStatus.COMPLETED)) {
            throw new BusinessException("Cannot register beneficiaries for a " + camp.getStatus() + " camp");
        }
        entity.setCampName(camp.getName());
        if (entity.getVillage() == null || entity.getVillage().isBlank()) {
            entity.setVillage(camp.getVillage());
        }
        if (entity.getRegisteredOn() == null) {
            entity.setRegisteredOn(LocalDate.now());
        }
    }
}
