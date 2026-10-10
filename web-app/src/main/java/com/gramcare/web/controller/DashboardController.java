package com.gramcare.web.controller;

import com.gramcare.web.client.ActionClient;
import com.gramcare.web.client.LenientMap;
import com.gramcare.web.client.ServiceUnavailableException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final ActionClient actionClient;

    public DashboardController(ActionClient actionClient) {
        this.actionClient = actionClient;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        Map<String, Object> summary = new LenientMap();
        List<Map<String, Object>> recent = List.of();
        List<Map<String, Object>> all = List.of();
        try {
            summary = actionClient.getDashboardSummary();
            recent = actionClient.recentActivities(8);
            all = actionClient.listActions(null, null, null, null);
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
        }

        List<Map<String, Object>> pending = all.stream()
                .filter(a -> !"RESOLVED".equals(a.get("status")) && !"CLOSED".equals(a.get("status")))
                .toList();

        List<Map<String, Object>> highPriority = pending.stream()
                .filter(a -> "CRITICAL".equals(a.get("priority")) || "HIGH".equals(a.get("priority")))
                .sorted(Comparator.comparing(a -> "CRITICAL".equals(a.get("priority")) ? 0 : 1))
                .limit(8)
                .toList();

        Map<String, Long> pendingByVillage = pending.stream().collect(Collectors.groupingBy(
                a -> a.get("village") == null ? "Unspecified" : a.get("village").toString(),
                TreeMap::new, Collectors.counting()));

        long maxCategory = 1;
        Object byCat = summary.get("actionsByCategory");
        if (byCat instanceof Map<?, ?> m) {
            for (Object v : m.values()) if (v instanceof Number n) maxCategory = Math.max(maxCategory, n.longValue());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> availability = summary.get("serviceAvailability") instanceof Map<?, ?> av
                ? (Map<String, Object>) av : Map.of();
        List<String> downServices = availability.entrySet().stream()
                .filter(e -> Boolean.FALSE.equals(e.getValue())).map(Map.Entry::getKey).toList();

        model.addAttribute("summary", summary);
        model.addAttribute("recentActivities", recent);
        model.addAttribute("highPriority", highPriority);
        model.addAttribute("pendingActions", pending.stream().limit(10).toList());
        model.addAttribute("pendingByVillage", pendingByVillage);
        model.addAttribute("maxCategory", maxCategory);
        model.addAttribute("downServices", downServices);
        return "dashboard";
    }
}
