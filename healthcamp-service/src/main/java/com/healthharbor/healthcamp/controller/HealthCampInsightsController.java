package com.healthharbor.healthcamp.controller;

import com.healthharbor.common.dto.PendingAction;
import com.healthharbor.healthcamp.entity.FollowUp;
import com.healthharbor.healthcamp.entity.FollowUp.FollowUpStatus;
import com.healthharbor.healthcamp.entity.HealthCamp;
import com.healthharbor.healthcamp.entity.Screening;
import com.healthharbor.healthcamp.repository.*;
import com.healthharbor.healthcamp.service.HealthCampService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/health-camps")
@RequiredArgsConstructor
@Tag(name = "Health camp insights")
public class HealthCampInsightsController {

    private static final Set<FollowUpStatus> OPEN = EnumSet.of(FollowUpStatus.PENDING, FollowUpStatus.IN_PROGRESS);

    private final HealthCampService campService;
    private final HealthCampRepository camps;
    private final CampStaffRepository staff;
    private final BeneficiaryRepository beneficiaries;
    private final ScreeningRepository screenings;
    private final FollowUpRepository followUps;

    @GetMapping("/{id}/summary")
    public Map<String, Object> summary(@PathVariable Long id) {
        HealthCamp camp = campService.get(id);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("camp", camp);
        m.put("staff", staff.countByCampId(id));
        m.put("beneficiaries", beneficiaries.countByCampId(id));
        m.put("screenings", screenings.countByCampId(id));
        m.put("abnormalScreenings", screenings.countByCampIdAndOutcomeNot(id, Screening.Outcome.NORMAL));
        m.put("followUps", followUps.countByCampId(id));
        m.put("openFollowUps", followUps.countByCampIdAndStatusIn(id, OPEN));
        return m;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        LocalDate today = LocalDate.now();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalCamps", camps.count());
        m.put("upcomingCamps", camps.findByStatusAndStartDateBetweenOrderByStartDate(HealthCamp.CampStatus.PLANNED, today, today.plusDays(30)).size());
        m.put("totalBeneficiaries", beneficiaries.count());
        m.put("totalScreenings", screenings.count());
        m.put("openFollowUps", followUps.findByStatusInOrderByDueDateAsc(OPEN).size());
        m.put("overdueFollowUps", followUps.findByStatusInOrderByDueDateAsc(OPEN).stream().filter(FollowUp::isOverdue).count());
        m.put("campsByStatus", toMap(camps.countByStatus()));
        m.put("campsByType", toMap(camps.countByType()));
        m.put("beneficiariesByCamp", toMap(beneficiaries.countByCamp()));
        m.put("screeningOutcomes", toMap(screenings.countByOutcome()));
        m.put("abnormalByType", toMap(screenings.abnormalByType()));
        m.put("followUpsByStatus", toMap(followUps.countByStatus()));
        return m;
    }

    @GetMapping("/pending")
    public List<PendingAction> pending() {
        LocalDate today = LocalDate.now();
        List<PendingAction> actions = new ArrayList<>();
        for (FollowUp f : followUps.findByStatusInOrderByDueDateAsc(OPEN)) {
            actions.add(new PendingAction("healthcamp", f.isOverdue() ? "Overdue Camp Follow-up" : "Camp Follow-up", f.getId(),
                    "FU-" + f.getId(), f.getBeneficiaryName() + ": " + f.getAction(), "From " + f.getCampName(),
                    f.getVillage(), f.isOverdue() && f.getPriority() != FollowUp.Priority.CRITICAL ? "HIGH" : f.getPriority().name(),
                    f.getStatus().name(), f.getDueDate(), f.getCreatedAt(), "/api/follow-ups/" + f.getId() + "/status",
                    f.getStatus() == FollowUpStatus.PENDING ? List.of("IN_PROGRESS", "COMPLETED", "MISSED") : List.of("COMPLETED", "MISSED"),
                    "/health-camps"));
        }
        for (HealthCamp c : camps.findByStatusAndStartDateBetweenOrderByStartDate(HealthCamp.CampStatus.PLANNED, today.minusDays(30), today.plusDays(7))) {
            actions.add(new PendingAction("healthcamp", "Upcoming Health Camp", c.getId(), c.getCampCode(),
                    c.getName() + " starts " + c.getStartDate(), c.getVenue(), c.getVillage(),
                    c.getStartDate().isBefore(today.plusDays(2)) ? "HIGH" : "MEDIUM", c.getStatus().name(), c.getStartDate(),
                    c.getCreatedAt(), "/api/health-camps/" + c.getId() + "/status", List.of("ONGOING", "CANCELLED"), "/health-camps"));
        }
        return actions;
    }

    private static Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        rows.forEach(r -> map.put(String.valueOf(r[0]), ((Number) r[1]).longValue()));
        return map;
    }
}
