package com.gramcare.web.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class ResidentClient extends AbstractApiClient {

    public ResidentClient(RestTemplate restTemplate, @Value("${resident-service.base-url}") String baseUrl) {
        super(restTemplate, baseUrl, "Resident & Health Centre Service");
    }

    public List<Map<String, Object>> listResidents(String name, String village) {
        StringBuilder sb = new StringBuilder("/api/residents?");
        if (name != null && !name.isBlank()) sb.append("name=").append(name).append("&");
        if (village != null && !village.isBlank()) sb.append("village=").append(village);
        return getList(sb.toString());
    }

    public Map<String, Object> getResident(Long id) { return getObject("/api/residents/" + id); }

    public Map<String, Object> createResident(Map<String, Object> body) { return post("/api/residents", body); }

    public Map<String, Object> updateResident(Long id, Map<String, Object> body) { return put("/api/residents/" + id, body); }

    public List<Map<String, Object>> getVisits(Long residentId) { return getList("/api/residents/" + residentId + "/visits"); }

    public Map<String, Object> addVisit(Long residentId, Map<String, Object> body) { return post("/api/residents/" + residentId + "/visits", body); }

    public long countResidents() { return getLong("/api/residents/count"); }

    public List<Map<String, Object>> listHealthCentres(String name, String village) {
        StringBuilder sb = new StringBuilder("/api/health-centres?");
        if (name != null && !name.isBlank()) sb.append("name=").append(name).append("&");
        if (village != null && !village.isBlank()) sb.append("village=").append(village);
        return getList(sb.toString());
    }

    public Map<String, Object> getHealthCentre(Long id) { return getObject("/api/health-centres/" + id); }

    public Map<String, Object> createHealthCentre(Map<String, Object> body) { return post("/api/health-centres", body); }

    public List<Map<String, Object>> getWorkers(Long centreId) { return getList("/api/health-centres/" + centreId + "/workers"); }

    public Map<String, Object> addWorker(Long centreId, Map<String, Object> body) { return post("/api/health-centres/" + centreId + "/workers", body); }

    public List<Map<String, Object>> getServices(Long centreId) { return getList("/api/health-centres/" + centreId + "/services"); }

    public Map<String, Object> addService(Long centreId, Map<String, Object> body) { return post("/api/health-centres/" + centreId + "/services", body); }

    public List<Map<String, Object>> getSchedules(Long centreId) { return getList("/api/health-centres/" + centreId + "/schedules"); }

    public Map<String, Object> addSchedule(Long centreId, Map<String, Object> body) { return post("/api/health-centres/" + centreId + "/schedules", body); }

    public long countActiveCentres() { return getLong("/api/health-centres/count-active"); }
}
