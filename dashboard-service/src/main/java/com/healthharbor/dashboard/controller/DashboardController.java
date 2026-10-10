package com.healthharbor.dashboard.controller;

import com.healthharbor.common.dto.PendingAction;
import com.healthharbor.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Community dashboard")
public class DashboardController {

    private final DashboardService dashboard;

    @GetMapping("/summary")
    @Operation(summary = "Consolidated KPIs from every microservice")
    public Map<String, Object> summary() {
        return dashboard.summary();
    }

    @GetMapping("/pending-actions")
    @Operation(summary = "All outstanding work across services, highest priority first")
    public List<PendingAction> pending(@RequestParam(required = false) String source,
                                       @RequestParam(required = false) String priority,
                                       @RequestParam(required = false) String village) {
        return dashboard.pendingActions().stream()
                .filter(a -> source == null || source.isBlank() || source.equalsIgnoreCase(a.source()))
                .filter(a -> priority == null || priority.isBlank() || priority.equalsIgnoreCase(a.priority()))
                .filter(a -> village == null || village.isBlank() || village.equalsIgnoreCase(a.village()))
                .toList();
    }

    @GetMapping("/reports/villages")
    @Operation(summary = "Village-wise community health & sanitation report")
    public List<Map<String, Object>> villageReport() {
        return dashboard.villageReport();
    }
}
