package com.gramcare.sanitation.config;

import com.gramcare.sanitation.service.SanitationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically retries the link to the Action Tracking Service for complaints
 * and flagged water observations saved while that service was unavailable.
 * This also means the services can be started in any order.
 */
@Component
public class ActionLinkReconciler {

    private static final Logger log = LoggerFactory.getLogger(ActionLinkReconciler.class);

    private final SanitationService sanitationService;

    public ActionLinkReconciler(SanitationService sanitationService) {
        this.sanitationService = sanitationService;
    }

    @Scheduled(initialDelay = 20000, fixedDelay = 60000)
    public void reconcile() {
        try {
            int stillUnlinked = sanitationService.reconcileUnlinked();
            if (stillUnlinked > 0) {
                log.info("{} sanitation/water record(s) still waiting for the Action Tracking Service", stillUnlinked);
            }
        } catch (Exception ex) {
            log.warn("Action link reconciliation failed: {}", ex.getMessage());
        }
    }
}
