package com.gramcare.resident;

import com.gramcare.resident.dto.ResidentRequest;
import com.gramcare.resident.exception.ResourceNotFoundException;
import com.gramcare.resident.service.ResidentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ResidentValidationTests {

    @Autowired
    private jakarta.validation.Validator validator;

    @Autowired
    private ResidentService residentService;

    @Test
    void residentWithoutNameOrVillageFailsValidation() {
        ResidentRequest req = new ResidentRequest();
        assertEquals(2, validator.validate(req).size());
    }

    @Test
    void unknownResidentIdRaisesNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> residentService.findById(999999L));
    }
}
