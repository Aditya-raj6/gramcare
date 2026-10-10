package com.gramcare.resident;

import com.gramcare.resident.dto.ResidentRequest;
import com.gramcare.resident.entity.Resident;
import com.gramcare.resident.repository.MedicalRecordRepository;
import com.gramcare.resident.repository.ResidentRepository;
import com.gramcare.resident.service.ResidentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ResidentServiceTests {

    @org.springframework.beans.factory.annotation.Autowired
    private ResidentService residentService;

    @Test
    void createsAndFindsResident() {
        ResidentRequest req = new ResidentRequest();
        req.setFullName("Lakshmi Devi");
        req.setVillage("Kondapur");
        Resident saved = residentService.create(req);
        assertNotNull(saved.getId());
        Resident found = residentService.findById(saved.getId());
        assertEquals("Lakshmi Devi", found.getFullName());
    }

    @Test
    void searchByVillageReturnsMatches() {
        ResidentRequest req = new ResidentRequest();
        req.setFullName("Ravi Kumar");
        req.setVillage("Anandpur");
        residentService.create(req);
        assertFalse(residentService.search(null, "Anandpur").isEmpty());
    }
}
