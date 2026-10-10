package com.gramcare.action.service;

import com.gramcare.action.dto.DashboardSummary;
import com.gramcare.action.entity.ActionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregates the figures shown on the Community Health Dashboard by calling
 * the other services' REST APIs. Every downstream call is wrapped so that one
 * unavailable service degrades its own figure to 0/"unavailable" instead of
 * failing the whole dashboard - the UI is told which services did not
 * respond via serviceAvailability rather than being given a silently wrong
 * number.
 */
@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final RestTemplate restTemplate;
    private final ActionItemService actionItemService;
    private final String residentServiceUrl;
    private final String inventoryServiceUrl;
    private final String sanitationServiceUrl;
    private final String appointmentCampServiceUrl;

    public DashboardService(RestTemplate restTemplate,
                             ActionItemService actionItemService,
                             @Value("${resident-service.base-url}") String residentServiceUrl,
                             @Value("${inventory-service.base-url}") String inventoryServiceUrl,
                             @Value("${sanitation-service.base-url}") String sanitationServiceUrl,
                             @Value("${appointment-camp-service.base-url}") String appointmentCampServiceUrl) {
        this.restTemplate = restTemplate;
        this.actionItemService = actionItemService;
        this.residentServiceUrl = residentServiceUrl;
        this.inventoryServiceUrl = inventoryServiceUrl;
        this.sanitationServiceUrl = sanitationServiceUrl;
        this.appointmentCampServiceUrl = appointmentCampServiceUrl;
    }

    public DashboardSummary getSummary() {
        DashboardSummary summary = new DashboardSummary();
        Map<String, Boolean> availability = new LinkedHashMap<>();

        summary.setTotalResidents(fetchCount(residentServiceUrl + "/api/residents/count", "resident-service", availability));
        summary.setActiveHealthCentres(fetchCount(residentServiceUrl + "/api/health-centres/count-active", "resident-service", availability));
        summary.setLowStockMedicines(fetchArraySize(inventoryServiceUrl + "/api/medicines/low-stock", "inventory-service", availability));
        summary.setPendingReplenishments(fetchArraySize(inventoryServiceUrl + "/api/replenishment-requests?status=PENDING", "inventory-service", availability));
        summary.setOpenSanitationComplaints(fetchArraySize(sanitationServiceUrl + "/api/sanitation/complaints?status=OPEN", "sanitation-service", availability));
        summary.setWaterQualityIssues(fetchArraySize(sanitationServiceUrl + "/api/water-quality/observations?reviewStatus=FLAGGED_FOR_REVIEW", "sanitation-service", availability));
        summary.setTodaysAppointments(fetchArraySize(appointmentCampServiceUrl + "/api/appointments/today", "appointment-camp-service", availability));
        summary.setActiveHealthCamps(fetchArraySize(appointmentCampServiceUrl + "/api/health-camps/active", "appointment-camp-service", availability));

        summary.setTotalPendingActions(actionItemService.countPending());
        summary.setActionsByCategory(groupByCategory());
        summary.setActionsByStatus(groupByStatus());
        summary.setServiceAvailability(availability);

        return summary;
    }

    private long fetchCount(String url, String serviceName, Map<String, Boolean> availability) {
        try {
            Long count = restTemplate.getForObject(url, Long.class);
            availability.put(serviceName, true);
            return count == null ? 0 : count;
        } catch (RestClientException ex) {
            log.warn("Could not reach {} at {}: {}", serviceName, url, ex.getMessage());
            availability.putIfAbsent(serviceName, false);
            return 0;
        }
    }

    private long fetchArraySize(String url, String serviceName, Map<String, Boolean> availability) {
        try {
            Object[] result = restTemplate.getForObject(url, Object[].class);
            availability.put(serviceName, true);
            return result == null ? 0 : result.length;
        } catch (RestClientException ex) {
            log.warn("Could not reach {} at {}: {}", serviceName, url, ex.getMessage());
            availability.putIfAbsent(serviceName, false);
            return 0;
        }
    }

    private Map<String, Long> groupByCategory() {
        return Arrays.stream(ActionItem.Category.values())
                .collect(Collectors.toMap(Enum::name, c -> (long) actionItemService.findAll(null, c.name(), null, null).size(),
                        (a, b) -> a, LinkedHashMap::new));
    }

    private Map<String, Long> groupByStatus() {
        return Arrays.stream(ActionItem.Status.values())
                .collect(Collectors.toMap(Enum::name, s -> (long) actionItemService.findAll(s.name(), null, null, null).size(),
                        (a, b) -> a, LinkedHashMap::new));
    }
}
