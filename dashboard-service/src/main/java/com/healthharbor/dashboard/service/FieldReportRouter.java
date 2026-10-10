package com.healthharbor.dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthharbor.dashboard.client.ServiceClients.InventoryClient;
import com.healthharbor.dashboard.client.ServiceClients.SanitationClient;
import com.healthharbor.dashboard.client.ServiceClients.WaterClient;
import com.healthharbor.dashboard.entity.FieldReport;
import com.healthharbor.dashboard.entity.FieldReportItem;
import com.healthharbor.dashboard.entity.FieldReportItem.RoutingStatus;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Routes each field-report item to the microservice that owns that kind of work and mirrors
 * the downstream record's status back onto the item.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FieldReportRouter {

    static final Set<String> TERMINAL = Set.of("COMPLETED", "CANCELLED", "RESOLVED", "CLOSED", "REJECTED", "FULFILLED");

    private final SanitationClient sanitation;
    private final InventoryClient inventory;
    private final WaterClient water;
    private final ObjectMapper mapper;

    public void route(FieldReport report, FieldReportItem item) {
        try {
            Map<String, Object> created = switch (item.getItemType()) {
                case SANITATION_MAINTENANCE -> routeMaintenance(report, item);
                case SANITATION_COMPLAINT -> routeComplaint(report, item);
                case MEDICINE_SHORTAGE -> routeMedicine(report, item);
                case WATER_ISSUE -> routeWater(report, item);
            };
            item.setTargetId(((Number) created.get("id")).longValue());
            item.setTargetReference(String.valueOf(created.get(referenceField(item.getItemType()))));
            item.setTargetStatus(String.valueOf(created.get("status")));
            item.setRoutingStatus(RoutingStatus.ROUTED);
            item.setRoutingMessage("Routed to " + item.getTargetService());
            item.setLastSyncedAt(LocalDateTime.now());
        } catch (FeignException ex) {
            item.setRoutingStatus(RoutingStatus.FAILED);
            item.setRoutingMessage(extractMessage(ex));
            log.warn("Routing of {} to {} failed: {}", item.getItemType(), item.getTargetService(), item.getRoutingMessage());
        } catch (RuntimeException ex) {
            item.setRoutingStatus(RoutingStatus.FAILED);
            item.setRoutingMessage(item.getTargetService() + " is unavailable: " + ex.getMessage());
            log.warn("Routing of {} failed", item.getItemType(), ex);
        }
    }

    /** Refreshes the mirrored status from the owning service; failures leave the last known status. */
    public void sync(FieldReportItem item) {
        if (item.getRoutingStatus() != RoutingStatus.ROUTED || item.getTargetId() == null
                || TERMINAL.contains(item.getTargetStatus())) {
            return;
        }
        try {
            Map<String, Object> target = switch (item.getItemType()) {
                case SANITATION_MAINTENANCE -> sanitation.maintenance(item.getTargetId());
                case SANITATION_COMPLAINT -> sanitation.complaint(item.getTargetId());
                case MEDICINE_SHORTAGE -> inventory.replenishment(item.getTargetId());
                case WATER_ISSUE -> water.issue(item.getTargetId());
            };
            item.setTargetStatus(String.valueOf(target.get("status")));
            item.setLastSyncedAt(LocalDateTime.now());
        } catch (RuntimeException ex) {
            log.debug("Status sync failed for item {}: {}", item.getId(), ex.getMessage());
        }
    }

    private Map<String, Object> routeMaintenance(FieldReport report, FieldReportItem item) {
        item.setTargetService("sanitation-service");
        item.setTargetPath("/sanitation");
        Map<String, Object> body = new HashMap<>();
        body.put("facilityId", item.getFacilityId());
        body.put("maintenanceType", item.getCategory() == null || item.getCategory().isBlank() ? "REPAIR" : item.getCategory());
        body.put("description", report.getReportNumber() + ": " + item.getDescription());
        body.put("priority", item.getPriority().name());
        body.put("source", "FIELD_REPORT");
        body.put("reportedBy", report.getReportedBy());
        body.put("scheduledDate", LocalDate.now().plusDays(item.getPriority() == FieldReportItem.Priority.CRITICAL ? 0 : 1).toString());
        return sanitation.createMaintenance(body);
    }

    private Map<String, Object> routeComplaint(FieldReport report, FieldReportItem item) {
        item.setTargetService("sanitation-service");
        item.setTargetPath("/sanitation");
        Map<String, Object> body = new HashMap<>();
        body.put("facilityId", item.getFacilityId());
        body.put("village", report.getVillage());
        body.put("category", item.getCategory() == null || item.getCategory().isBlank() ? "OTHER" : item.getCategory());
        body.put("description", report.getReportNumber() + ": " + item.getDescription());
        body.put("complainantName", report.getReportedBy());
        body.put("priority", item.getPriority().name());
        body.put("channel", "FIELD_REPORT");
        return sanitation.createComplaint(body);
    }

    private Map<String, Object> routeMedicine(FieldReport report, FieldReportItem item) {
        item.setTargetService("inventory-service");
        item.setTargetPath("/inventory");
        Map<String, Object> body = new HashMap<>();
        body.put("medicineId", item.getMedicineId());
        body.put("healthCentreId", item.getHealthCentreId());
        body.put("healthCentreName", item.getHealthCentreName());
        body.put("requestedQuantity", item.getQuantity() == null ? 100 : item.getQuantity());
        body.put("priority", item.getPriority().name());
        body.put("source", "FIELD_REPORT");
        body.put("requestedBy", report.getReportedBy());
        body.put("reason", report.getReportNumber() + ": " + item.getDescription());
        body.put("requiredBy", LocalDate.now().plusDays(item.getPriority() == FieldReportItem.Priority.CRITICAL ? 2 : 7).toString());
        return inventory.createReplenishment(body);
    }

    private Map<String, Object> routeWater(FieldReport report, FieldReportItem item) {
        item.setTargetService("water-service");
        item.setTargetPath("/water");
        String description = item.getDescription();
        Map<String, Object> body = new HashMap<>();
        body.put("sourceId", item.getWaterSourceId());
        body.put("village", report.getVillage());
        body.put("category", item.getCategory() == null || item.getCategory().isBlank() ? "OTHER" : item.getCategory());
        body.put("title", description.length() > 80 ? description.substring(0, 77) + "..." : description);
        body.put("description", report.getReportNumber() + ": " + description);
        body.put("severity", item.getPriority().name());
        body.put("reportedBy", report.getReportedBy());
        body.put("source", "FIELD_REPORT");
        return water.createIssue(body);
    }

    private static String referenceField(FieldReportItem.ItemType type) {
        return switch (type) {
            case SANITATION_MAINTENANCE -> "taskNumber";
            case SANITATION_COMPLAINT -> "complaintNumber";
            case MEDICINE_SHORTAGE -> "requestNumber";
            case WATER_ISSUE -> "issueNumber";
        };
    }

    private String extractMessage(FeignException ex) {
        try {
            JsonNode node = mapper.readTree(ex.contentUTF8());
            if (node.hasNonNull("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ignored) {
            // fall through to a generic message
        }
        return "Downstream service returned HTTP " + ex.status();
    }
}
