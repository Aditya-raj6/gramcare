package com.gramcare.web.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class InventoryClient extends AbstractApiClient {

    public InventoryClient(RestTemplate restTemplate, @Value("${inventory-service.base-url}") String baseUrl) {
        super(restTemplate, baseUrl, "Medicine & Inventory Service");
    }

    public List<Map<String, Object>> listMedicines(String name) {
        return getList("/api/medicines" + (name != null && !name.isBlank() ? "?name=" + name : ""));
    }

    public Map<String, Object> getMedicine(Long id) { return getObject("/api/medicines/" + id); }

    public List<Map<String, Object>> lowStock() { return getList("/api/medicines/low-stock"); }

    public Map<String, Object> createMedicine(Map<String, Object> body) { return post("/api/medicines", body); }

    public Map<String, Object> stockIn(Long medicineId, Map<String, Object> body) { return post("/api/medicines/" + medicineId + "/stock-in", body); }

    public Map<String, Object> issueStock(Long medicineId, Map<String, Object> body) { return post("/api/medicines/" + medicineId + "/issue", body); }

    public List<Map<String, Object>> batches(Long medicineId) { return getList("/api/medicines/" + medicineId + "/batches"); }

    public Map<String, Object> retryReplenishmentLink(Long id) { return post("/api/replenishment-requests/" + id + "/retry-action-link", Map.of()); }

    public List<Map<String, Object>> transactionHistory(Long medicineId) { return getList("/api/medicines/" + medicineId + "/transactions"); }

    public List<Map<String, Object>> recentTransactions() { return getList("/api/stock-transactions/recent"); }

    public List<Map<String, Object>> replenishmentRequests(String status) {
        return getList("/api/replenishment-requests" + (status != null && !status.isBlank() ? "?status=" + status : ""));
    }

    public Map<String, Object> createReplenishmentRequest(Map<String, Object> body) { return post("/api/replenishment-requests", body); }

    public Map<String, Object> updateReplenishmentStatus(Long id, Map<String, Object> body) { return put("/api/replenishment-requests/" + id + "/status", body); }
}
