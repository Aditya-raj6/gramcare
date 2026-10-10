package com.gramcare.action.controller;

import com.gramcare.action.dto.ActionItemResponse;
import com.gramcare.action.dto.DashboardSummary;
import com.gramcare.action.service.ActionItemService;
import com.gramcare.action.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final ActionItemService actionItemService;

    public DashboardController(DashboardService dashboardService, ActionItemService actionItemService) {
        this.dashboardService = dashboardService;
        this.actionItemService = actionItemService;
    }

    @GetMapping("/summary")
    public DashboardSummary summary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/recent-activities")
    public List<ActionItemResponse> recentActivities(@RequestParam(defaultValue = "10") int limit) {
        return actionItemService.findRecent(limit).stream().map(ActionItemResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/critical-actions")
    public List<ActionItemResponse> criticalActions() {
        return actionItemService.findCritical().stream().map(ActionItemResponse::from).collect(Collectors.toList());
    }
}
