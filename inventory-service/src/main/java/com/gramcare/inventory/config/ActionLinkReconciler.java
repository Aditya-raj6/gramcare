package com.gramcare.inventory.config;

import com.gramcare.inventory.service.MedicineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically retries the link to the Action Tracking Service for
 * replenishment requests saved while that service was unavailable. Because
 * action creation is idempotent, retrying is always safe.
 */
@Component
public class ActionLinkReconciler {

    private static final Logger log = LoggerFactory.getLogger(ActionLinkReconciler.class);

    private final MedicineService medicineService;

    public ActionLinkReconciler(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @Scheduled(initialDelay = 20000, fixedDelay = 60000)
    public void reconcile() {
        try {
            int stillUnlinked = medicineService.reconcileUnlinked();
            if (stillUnlinked > 0) {
                log.info("{} replenishment request(s) still waiting for the Action Tracking Service", stillUnlinked);
            }
        } catch (Exception ex) {
            log.warn("Action link reconciliation failed: {}", ex.getMessage());
        }
    }
}
