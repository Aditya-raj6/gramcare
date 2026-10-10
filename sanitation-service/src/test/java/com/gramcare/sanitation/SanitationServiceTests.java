package com.gramcare.sanitation;

import com.gramcare.sanitation.dto.ComplaintRequest;
import com.gramcare.sanitation.dto.WaterObservationRequest;
import com.gramcare.sanitation.entity.SanitationComplaint;
import com.gramcare.sanitation.entity.WaterQualityObservation;
import com.gramcare.sanitation.service.SanitationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SanitationServiceTests {

    @Autowired
    private SanitationService sanitationService;

    @Test
    void complaintIsCreatedEvenWhenActionServiceIsUnreachable() {
        ComplaintRequest req = new ComplaintRequest();
        req.setTitle("Toilet block needs cleaning");
        req.setDescription("Overflowing drain near the community toilet");
        req.setReportedBy("Health Worker Asha");
        SanitationComplaint saved = sanitationService.reportComplaint(req);

        assertNotNull(saved.getId());
        // The action-service is not running in this test, so the link is
        // expected to be absent rather than the whole operation failing.
        assertNull(saved.getActionItemId());
    }

    @Test
    void outOfRangeWaterObservationIsFlagged() {
        WaterObservationRequest req = new WaterObservationRequest();
        req.setVillage("Anandpur");
        req.setWaterSourceName("Hand pump 3");
        req.setPh(BigDecimal.valueOf(9.8));
        WaterQualityObservation saved = sanitationService.recordObservation(req);

        assertEquals(WaterQualityObservation.ReviewStatus.FLAGGED_FOR_REVIEW, saved.getReviewStatus());
    }

    @Test
    void normalWaterObservationIsNotFlagged() {
        WaterObservationRequest req = new WaterObservationRequest();
        req.setVillage("Anandpur");
        req.setWaterSourceName("Hand pump 1");
        req.setPh(BigDecimal.valueOf(7.2));
        req.setTurbidityNtu(BigDecimal.valueOf(2));
        req.setResidualChlorineMgL(BigDecimal.valueOf(0.4));
        WaterQualityObservation saved = sanitationService.recordObservation(req);

        assertEquals(WaterQualityObservation.ReviewStatus.NOT_FLAGGED, saved.getReviewStatus());
    }
}
