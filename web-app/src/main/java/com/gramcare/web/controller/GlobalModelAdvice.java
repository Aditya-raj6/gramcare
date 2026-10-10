package com.gramcare.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Makes the demo role and current path available to every Thymeleaf template, for the top bar and sidebar highlighting. */
@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("demoRole")
    public String demoRole(HttpServletRequest request) {
        Object role = request.getSession().getAttribute("demoRole");
        return role != null ? role.toString() : null;
    }

    @ModelAttribute("actionStatuses")
    public java.util.List<String> actionStatuses() {
        return java.util.List.of("OPEN", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED");
    }

    @ModelAttribute("actionCategories")
    public java.util.List<String> actionCategories() {
        return java.util.List.of("MEDICINE_STOCK", "SANITATION", "WATER_QUALITY", "HEALTH_CAMP", "GENERAL_HEALTH");
    }

    @ModelAttribute("priorities")
    public java.util.List<String> priorities() {
        return java.util.List.of("CRITICAL", "HIGH", "MEDIUM", "LOW");
    }

    @ModelAttribute("waterReviewStatuses")
    public java.util.List<String> waterReviewStatuses() {
        return java.util.List.of("NOT_FLAGGED", "FLAGGED_FOR_REVIEW", "ACTION_TAKEN", "CLEARED");
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
