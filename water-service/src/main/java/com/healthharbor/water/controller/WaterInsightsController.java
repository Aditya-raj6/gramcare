package com.healthharbor.water.controller;

import com.healthharbor.common.dto.PendingAction;
import com.healthharbor.water.entity.CorrectiveAction;
import com.healthharbor.water.entity.PublicHealthIssue;
import com.healthharbor.water.entity.PublicHealthIssue.IssueStatus;
import com.healthharbor.water.repository.CorrectiveActionRepository;
import com.healthharbor.water.repository.PublicHealthIssueRepository;
import com.healthharbor.water.repository.WaterQualityTestRepository;
import com.healthharbor.water.repository.WaterSourceRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/water")
@RequiredArgsConstructor
@Tag(name = "Water & public health insights")
public class WaterInsightsController {

    private static final Set<IssueStatus> OPEN = EnumSet.of(IssueStatus.OPEN, IssueStatus.INVESTIGATING, IssueStatus.ACTION_TAKEN);
    private static final Set<CorrectiveAction.ActionStatus> ACTIVE = EnumSet.of(CorrectiveAction.ActionStatus.PLANNED,
            CorrectiveAction.ActionStatus.IN_PROGRESS);

    private final WaterSourceRepository sources;
    private final WaterQualityTestRepository tests;
    private final PublicHealthIssueRepository issues;
    private final CorrectiveActionRepository actions;

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Long> quality = toMap(sources.countByQuality());
        Map<String, Long> status = toMap(sources.countByStatus());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalSources", sources.count());
        m.put("functionalSources", status.getOrDefault("FUNCTIONAL", 0L));
        m.put("contaminatedSources", status.getOrDefault("CONTAMINATED", 0L));
        m.put("unsafeSources", quality.getOrDefault("UNSAFE", 0L));
        m.put("populationServed", sources.totalPopulationServed());
        m.put("testsLast30Days", tests.countByTestDateGreaterThanEqual(LocalDate.now().minusDays(30)));
        m.put("openIssues", issues.findByStatusInOrderByIdDesc(OPEN).size());
        m.put("activeCorrectiveActions", actions.findByStatusInOrderByPlannedDateAsc(ACTIVE).size());
        m.put("sourcesByStatus", status);
        m.put("sourcesByQuality", quality);
        m.put("sourcesByType", toMap(sources.countByType()));
        m.put("testResults", toMap(tests.countByStatus()));
        m.put("unsafeTestsByVillage", toMap(tests.unsafeByVillage()));
        m.put("issuesByCategory", toMap(issues.countByCategory()));
        m.put("issuesByStatus", toMap(issues.countByStatus()));
        return m;
    }

    @GetMapping("/pending")
    public List<PendingAction> pending() {
        List<PendingAction> list = new ArrayList<>();
        for (PublicHealthIssue i : issues.findByStatusInOrderByIdDesc(OPEN)) {
            list.add(new PendingAction("water", "Water / Public Health Issue", i.getId(), i.getIssueNumber(), i.getTitle(),
                    i.getDescription(), i.getVillage(), i.getSeverity().name(), i.getStatus().name(), null, i.getCreatedAt(),
                    "/api/water/issues/" + i.getId() + "/status",
                    switch (i.getStatus()) {
                        case OPEN -> List.of("INVESTIGATING", "ACTION_TAKEN", "RESOLVED");
                        case INVESTIGATING -> List.of("ACTION_TAKEN", "RESOLVED");
                        default -> List.of("RESOLVED", "CLOSED");
                    }, "/water"));
        }
        LocalDate today = LocalDate.now();
        for (CorrectiveAction a : actions.findByStatusInOrderByPlannedDateAsc(ACTIVE)) {
            boolean overdue = a.getPlannedDate() != null && a.getPlannedDate().isBefore(today);
            list.add(new PendingAction("water", "Corrective Action", a.getId(), a.getIssueNumber() + "/CA-" + a.getId(),
                    a.getActionType().name().replace('_', ' ').toLowerCase() + ": " + a.getDescription(), null, a.getVillage(),
                    overdue ? "HIGH" : "MEDIUM", a.getStatus().name(), a.getPlannedDate(), a.getCreatedAt(),
                    "/api/water/actions/" + a.getId() + "/status",
                    a.getStatus() == CorrectiveAction.ActionStatus.PLANNED ? List.of("IN_PROGRESS", "COMPLETED", "CANCELLED")
                            : List.of("COMPLETED", "CANCELLED"), "/water"));
        }
        return list;
    }

    private static Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        rows.forEach(r -> map.put(String.valueOf(r[0]), r[1] == null ? 0L : ((Number) r[1]).longValue()));
        return map;
    }
}
