package com.healthharbor.dashboard.client;

import com.healthharbor.common.dto.PendingAction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/** Feign clients for every downstream microservice, resolved through Eureka. */
public final class ServiceClients {

    private ServiceClients() {
    }

    @FeignClient(name = "patient-service", contextId = "patientClient")
    public interface PatientClient {
        @GetMapping("/api/residents/stats")
        Map<String, Object> stats();
    }

    @FeignClient(name = "healthcenter-service", contextId = "healthCentreClient")
    public interface HealthCentreClient {
        @GetMapping("/api/health-centres/stats")
        Map<String, Object> stats();

        @GetMapping("/api/health-centres/pending")
        List<PendingAction> pending();

        @GetMapping("/api/health-centres/{id}")
        Map<String, Object> centre(@PathVariable("id") Long id);
    }

    @FeignClient(name = "appointment-service", contextId = "appointmentClient")
    public interface AppointmentClient {
        @GetMapping("/api/appointments/stats")
        Map<String, Object> stats();

        @GetMapping("/api/appointments/pending")
        List<PendingAction> pending();
    }

    @FeignClient(name = "inventory-service", contextId = "inventoryClient")
    public interface InventoryClient {
        @GetMapping("/api/inventory/stats")
        Map<String, Object> stats();

        @GetMapping("/api/inventory/pending")
        List<PendingAction> pending();

        @GetMapping("/api/stock/attention")
        List<Map<String, Object>> attention();

        @PostMapping("/api/replenishments")
        Map<String, Object> createReplenishment(@RequestBody Map<String, Object> request);

        @GetMapping("/api/replenishments/{id}")
        Map<String, Object> replenishment(@PathVariable("id") Long id);
    }

    @FeignClient(name = "healthcamp-service", contextId = "healthCampClient")
    public interface HealthCampClient {
        @GetMapping("/api/health-camps/stats")
        Map<String, Object> stats();

        @GetMapping("/api/health-camps/pending")
        List<PendingAction> pending();
    }

    @FeignClient(name = "sanitation-service", contextId = "sanitationClient")
    public interface SanitationClient {
        @GetMapping("/api/sanitation/stats")
        Map<String, Object> stats();

        @GetMapping("/api/sanitation/pending")
        List<PendingAction> pending();

        @PostMapping("/api/sanitation/maintenance")
        Map<String, Object> createMaintenance(@RequestBody Map<String, Object> request);

        @GetMapping("/api/sanitation/maintenance/{id}")
        Map<String, Object> maintenance(@PathVariable("id") Long id);

        @PostMapping("/api/sanitation/complaints")
        Map<String, Object> createComplaint(@RequestBody Map<String, Object> request);

        @GetMapping("/api/sanitation/complaints/{id}")
        Map<String, Object> complaint(@PathVariable("id") Long id);
    }

    @FeignClient(name = "water-service", contextId = "waterClient")
    public interface WaterClient {
        @GetMapping("/api/water/stats")
        Map<String, Object> stats();

        @GetMapping("/api/water/pending")
        List<PendingAction> pending();

        @PostMapping("/api/water/issues")
        Map<String, Object> createIssue(@RequestBody Map<String, Object> request);

        @GetMapping("/api/water/issues/{id}")
        Map<String, Object> issue(@PathVariable("id") Long id);
    }
}
