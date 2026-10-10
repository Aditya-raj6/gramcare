package com.healthharbor.healthcamp.service;

import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.healthcamp.entity.Beneficiary;
import com.healthharbor.healthcamp.entity.HealthCamp;
import com.healthharbor.healthcamp.repository.BeneficiaryRepository;
import com.healthharbor.healthcamp.repository.HealthCampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CampResolver {
    private final HealthCampRepository camps;
    private final BeneficiaryRepository beneficiaries;

    public HealthCamp camp(Long id) {
        return camps.findById(id).orElseThrow(() -> new BusinessException("Health camp " + id + " does not exist"));
    }

    public Beneficiary beneficiary(Long id) {
        return beneficiaries.findById(id).orElseThrow(() -> new BusinessException("Beneficiary " + id + " does not exist"));
    }
}
