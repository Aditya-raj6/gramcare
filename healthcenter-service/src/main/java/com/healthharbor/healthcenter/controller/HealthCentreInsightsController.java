package com.healthharbor.healthcenter.controller;

import com.healthharbor.common.dto.PendingAction;
import com.healthharbor.healthcenter.entity.Facility;
import com.healthharbor.healthcenter.entity.HealthCentre;
import com.healthharbor.healthcenter.entity.HealthWorker;
import com.healthharbor.healthcenter.repository.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/health-centres")
@RequiredArgsConstructor
@Tag(name = "Health centre insights")
public class HealthCentreInsightsController {

    private final HealthCentreRepository centres;
    private final FacilityRepository facilities;
    private final HealthWorkerRepository workers;
    private final ServiceOfferingRepository services;

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCentres", centres.count());
        stats.put("totalWorkers", workers.count());
        stats.put("activeWorkers", workers.countByStatus(HealthWorker.WorkerStatus.ACTIVE));
        stats.put("workersOnLeave", workers.countByStatus(HealthWorker.WorkerStatus.ON_LEAVE));
        stats.put("totalFacilities", facilities.count());
        stats.put("totalServices", services.count());
        stats.put("centresByType", toMap(centres.countByType()));
        stats.put("centresByStatus", toMap(centres.countByStatus()));
        stats.put("workersByRole", toMap(workers.countByRole()));
        stats.put("facilitiesByStatus", toMap(facilities.countByStatus()));
        return stats;
    }

    @GetMapping("/pending")
    public List<PendingAction> pending() {
        List<PendingAction> actions = new ArrayList<>();
        for (Facility f : facilities.findByStatusNot(Facility.FacilityStatus.AVAILABLE)) {
            actions.add(new PendingAction("healthcenter", "Facility Issue", f.getId(), "FAC-" + f.getId(),
                    f.getName() + " - " + f.getStatus().name().replace('_', ' ').toLowerCase(), f.getNotes(),
                    f.getCentreName(), f.getStatus() == Facility.FacilityStatus.UNAVAILABLE ? "HIGH" : "MEDIUM",
                    f.getStatus().name(), null, f.getUpdatedAt(), "/api/facilities/" + f.getId() + "/status",
                    List.of("AVAILABLE", "UNDER_MAINTENANCE", "UNAVAILABLE"), "/health-centres"));
        }
        for (HealthCentre c : centres.findByStatusNot(HealthCentre.CentreStatus.OPERATIONAL)) {
            actions.add(new PendingAction("healthcenter", "Centre Capacity", c.getId(), c.getCode(),
                    c.getName() + " operating with " + c.getStatus().name().toLowerCase() + " capacity", null,
                    c.getVillage(), c.getStatus() == HealthCentre.CentreStatus.CLOSED ? "CRITICAL" : "HIGH",
                    c.getStatus().name(), null, c.getUpdatedAt(), "/api/health-centres/" + c.getId() + "/status",
                    List.of("OPERATIONAL", "LIMITED", "CLOSED"), "/health-centres"));
        }
        return actions;
    }

    private static Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        rows.forEach(r -> map.put(String.valueOf(r[0]), ((Number) r[1]).longValue()));
        return map;
    }
}
