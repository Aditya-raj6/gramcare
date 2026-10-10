package com.gramcare.web.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class SanitationClient extends AbstractApiClient {

    public SanitationClient(RestTemplate restTemplate, @Value("${sanitation-service.base-url}") String baseUrl) {
        super(restTemplate, baseUrl, "Sanitation & Public Health Service");
    }

    public List<Map<String, Object>> listFacilities() { return getList("/api/sanitation/facilities"); }

    public Map<String, Object> getFacility(Long id) { return getObject("/api/sanitation/facilities/" + id); }

    public Map<String, Object> createFacility(Map<String, Object> body) { return post("/api/sanitation/facilities", body); }

    public Map<String, Object> addInspection(Long facilityId, Map<String, Object> body) { return post("/api/sanitation/facilities/" + facilityId + "/inspections", body); }

    public List<Map<String, Object>> getInspections(Long facilityId) { return getList("/api/sanitation/facilities/" + facilityId + "/inspections"); }

    public List<Map<String, Object>> listComplaints(String status) {
        return getList("/api/sanitation/complaints" + (status != null && !status.isBlank() ? "?status=" + status : ""));
    }

    public Map<String, Object> getComplaint(Long id) { return getObject("/api/sanitation/complaints/" + id); }

    public Map<String, Object> createComplaint(Map<String, Object> body) { return post("/api/sanitation/complaints", body); }

    public Map<String, Object> updateComplaintStatus(Long id, Map<String, Object> body) { return put("/api/sanitation/complaints/" + id + "/status", body); }

    public Map<String, Object> retryActionLink(Long id) { return post("/api/sanitation/complaints/" + id + "/retry-action-link", Map.of()); }

    public List<Map<String, Object>> listObservations(String village) {
        return getList("/api/water-quality/observations" + (village != null && !village.isBlank() ? "?village=" + village : ""));
    }

    public Map<String, Object> getObservation(Long id) { return getObject("/api/water-quality/observations/" + id); }

    public Map<String, Object> createObservation(Map<String, Object> body) { return post("/api/water-quality/observations", body); }

    public Map<String, Object> addCorrectiveAction(Long observationId, Map<String, Object> body) {
        return post("/api/water-quality/observations/" + observationId + "/corrective-actions", body);
    }

    public List<Map<String, Object>> getCorrectiveActions(Long observationId) {
        return getList("/api/water-quality/observations/" + observationId + "/corrective-actions");
    }

    public List<Map<String, Object>> flaggedObservations() {
        return getList("/api/water-quality/observations?reviewStatus=FLAGGED_FOR_REVIEW");
    }

    public Map<String, Object> updateReviewStatus(Long id, Map<String, Object> body) {
        return put("/api/water-quality/observations/" + id + "/review-status", body);
    }

    public Map<String, Object> retryObservationLink(Long id) {
        return post("/api/water-quality/observations/" + id + "/retry-action-link", Map.of());
    }
}
