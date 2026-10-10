package com.healthharbor.patient.service;

import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.patient.entity.Resident;
import com.healthharbor.patient.repository.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Validates resident references and resolves the display name stored on child records. */
@Component
@RequiredArgsConstructor
public class ResidentNameResolver {
    private final ResidentRepository residents;

    public String resolve(Long residentId) {
        return residents.findById(residentId).map(Resident::getFullName)
                .orElseThrow(() -> new BusinessException("Resident " + residentId + " does not exist"));
    }
}
