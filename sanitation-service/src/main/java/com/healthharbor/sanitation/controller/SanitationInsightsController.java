package com.healthharbor.sanitation.controller;

import com.healthharbor.common.dto.PendingAction;
import com.healthharbor.sanitation.entity.Complaint;
import com.healthharbor.sanitation.entity.Complaint.ComplaintStatus;
import com.healthharbor.sanitation.entity.MaintenanceTask;
import com.healthharbor.sanitation.entity.MaintenanceTask.TaskStatus;
import com.healthharbor.sanitation.repository.ComplaintRepository;
import com.healthharbor.sanitation.repository.InspectionRepository;
import com.healthharbor.sanitation.repository.MaintenanceTaskRepository;
import com.healthharbor.sanitation.repository.SanitationFacilityRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/sanitation")
@RequiredArgsConstructor
@Tag(name = "Sanitation insights")
public class SanitationInsightsController {

    private static final Set<TaskStatus> ACTIVE = EnumSet.of(TaskStatus.PENDING, TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS);
    private static final Set<ComplaintStatus> OPEN = EnumSet.of(ComplaintStatus.OPEN, ComplaintStatus.IN_PROGRESS);

    private final SanitationFacilityRepository facilities;
    private final InspectionRepository inspections;
    private final MaintenanceTaskRepository tasks;
    private final ComplaintRepository complaints;

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> m = new LinkedHashMap<>();
        Map<String, Long> cleanliness = toMap(facilities.countByCleanliness());
        Map<String, Long> status = toMap(facilities.countByStatus());
        m.put("totalFacilities", facilities.count());
        m.put("operational", status.getOrDefault("OPERATIONAL", 0L));
        m.put("needsAttention", cleanliness.getOrDefault("DIRTY", 0L) + cleanliness.getOrDefault("UNUSABLE", 0L)
                + status.getOrDefault("NON_FUNCTIONAL", 0L));
        m.put("activeMaintenance", tasks.findByStatusInOrderByIdDesc(ACTIVE).size());
        m.put("openComplaints", complaints.findByStatusInOrderByIdDesc(OPEN).size());
        m.put("totalInspections", inspections.count());
        m.put("facilitiesByCleanliness", cleanliness);
        m.put("facilitiesByStatus", status);
        m.put("facilitiesByType", toMap(facilities.countByType()));
        m.put("maintenanceByStatus", toMap(tasks.countByStatus()));
        m.put("maintenanceByType", toMap(tasks.countByType()));
        m.put("complaintsByCategory", toMap(complaints.countByCategory()));
        m.put("complaintsByStatus", toMap(complaints.countByStatus()));
        m.put("complaintsByVillage", toMap(complaints.countByVillage()));
        return m;
    }

    @GetMapping("/pending")
    public List<PendingAction> pending() {
        List<PendingAction> actions = new ArrayList<>();
        for (MaintenanceTask t : tasks.findByStatusInOrderByIdDesc(ACTIVE)) {
            actions.add(new PendingAction("sanitation", "Sanitation Maintenance", t.getId(), t.getTaskNumber(),
                    t.getMaintenanceType().name().replace('_', ' ').toLowerCase() + " - " + t.getFacilityName(),
                    t.getDescription(), t.getVillage(), t.getPriority().name(), t.getStatus().name(), t.getScheduledDate(),
                    t.getCreatedAt(), "/api/sanitation/maintenance/" + t.getId() + "/status",
                    switch (t.getStatus()) {
                        case PENDING -> List.of("ASSIGNED", "IN_PROGRESS", "CANCELLED");
                        case ASSIGNED -> List.of("IN_PROGRESS", "CANCELLED");
                        default -> List.of("COMPLETED", "CANCELLED");
                    }, "/sanitation"));
        }
        for (Complaint c : complaints.findByStatusInOrderByIdDesc(OPEN)) {
            if (c.getMaintenanceTaskId() != null) {
                continue; // tracked through its maintenance task
            }
            actions.add(new PendingAction("sanitation", "Sanitation Complaint", c.getId(), c.getComplaintNumber(),
                    c.getCategory().name().replace('_', ' ').toLowerCase() + (c.getFacilityName() == null ? "" : " - " + c.getFacilityName()),
                    c.getDescription(), c.getVillage(), c.getPriority().name(), c.getStatus().name(), null, c.getCreatedAt(),
                    "/api/sanitation/complaints/" + c.getId() + "/status", List.of("IN_PROGRESS", "RESOLVED", "REJECTED"), "/sanitation"));
        }
        return actions;
    }

    private static Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        rows.forEach(r -> map.put(String.valueOf(r[0]), ((Number) r[1]).longValue()));
        return map;
    }
}
