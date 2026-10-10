package com.gramcare.web.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class ActionClient extends AbstractApiClient {

    public ActionClient(RestTemplate restTemplate, @Value("${action-service.base-url}") String baseUrl) {
        super(restTemplate, baseUrl, "Community Dashboard & Action Tracking Service");
    }

    public Map<String, Object> getDashboardSummary() { return getObject("/api/dashboard/summary"); }

    public List<Map<String, Object>> recentActivities(int limit) { return getList("/api/dashboard/recent-activities?limit=" + limit); }

    public List<Map<String, Object>> criticalActions() { return getList("/api/dashboard/critical-actions"); }

    public List<Map<String, Object>> listActions(String status, String category, String village, String priority) {
        StringBuilder sb = new StringBuilder("/api/actions?");
        if (status != null && !status.isBlank()) sb.append("status=").append(status).append("&");
        if (category != null && !category.isBlank()) sb.append("category=").append(category).append("&");
        if (village != null && !village.isBlank()) sb.append("village=").append(village).append("&");
        if (priority != null && !priority.isBlank()) sb.append("priority=").append(priority);
        return getList(sb.toString());
    }

    public Map<String, Object> create(Map<String, Object> body) { return post("/api/actions", body); }

    public List<Map<String, Object>> departments() { return getList("/api/actions/departments"); }

    public Map<String, Object> getAction(Long id) { return getObject("/api/actions/" + id); }

    public Map<String, Object> assign(Long id, Map<String, Object> body) { return put("/api/actions/" + id + "/assign", body); }

    public Map<String, Object> updateStatus(Long id, Map<String, Object> body) { return put("/api/actions/" + id + "/status", body); }

    public List<Map<String, Object>> getHistory(Long id) { return getList("/api/actions/" + id + "/history"); }
}
