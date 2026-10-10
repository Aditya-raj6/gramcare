package com.gramcare.action;

import com.gramcare.action.dto.ActionItemRequest;
import com.gramcare.action.dto.DashboardSummary;
import com.gramcare.action.dto.StatusUpdateRequest;
import com.gramcare.action.entity.ActionItem;
import com.gramcare.action.service.ActionItemService;
import com.gramcare.action.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DashboardServiceTests {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ActionItemService actionItemService;

    private ActionItem create(ActionItem.Category category) {
        ActionItemRequest req = new ActionItemRequest();
        req.setTitle("Test " + category);
        req.setCategory(category);
        req.setSourceService("test");
        req.setSourceReferenceType("TEST");
        req.setSourceReferenceId(System.nanoTime());
        return actionItemService.createOrGetExisting(req);
    }

    @Test
    void summaryCountsPendingActionsAndDegradesWhenOtherServicesAreDown() {
        long before = dashboardService.getSummary().getTotalPendingActions();

        create(ActionItem.Category.MEDICINE_STOCK);
        ActionItem resolved = create(ActionItem.Category.SANITATION);
        StatusUpdateRequest done = new StatusUpdateRequest();
        done.setStatus(ActionItem.Status.RESOLVED);
        actionItemService.updateStatus(resolved.getId(), done);

        DashboardSummary summary = dashboardService.getSummary();

        // Only the unresolved item adds to the pending count.
        assertEquals(before + 1, summary.getTotalPendingActions());
        assertTrue(summary.getActionsByCategory().get("MEDICINE_STOCK") >= 1);
        // No other service runs during tests: their figures fall back to 0 and
        // are reported as unavailable instead of failing the whole summary.
        assertEquals(0, summary.getTotalResidents());
        assertEquals(Boolean.FALSE, summary.getServiceAvailability().get("resident-service"));
    }
}
