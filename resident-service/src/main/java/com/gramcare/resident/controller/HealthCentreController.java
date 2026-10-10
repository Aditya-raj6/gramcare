package com.gramcare.resident.controller;

import com.gramcare.resident.dto.HealthCentreRequest;
import com.gramcare.resident.entity.*;
import com.gramcare.resident.service.HealthCentreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health-centres")
public class HealthCentreController {

    private final HealthCentreService healthCentreService;

    public HealthCentreController(HealthCentreService healthCentreService) {
        this.healthCentreService = healthCentreService;
    }

    @GetMapping
    public List<HealthCentre> list(@RequestParam(required = false) String name,
                                    @RequestParam(required = false) String village) {
        if (name != null || village != null) return healthCentreService.search(name, village);
        return healthCentreService.findAll();
    }

    @GetMapping("/{id}")
    public HealthCentre get(@PathVariable Long id) { return healthCentreService.findById(id); }

    @PostMapping
    public ResponseEntity<HealthCentre> create(@Valid @RequestBody HealthCentreRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(healthCentreService.create(req));
    }

    @PutMapping("/{id}")
    public HealthCentre update(@PathVariable Long id, @Valid @RequestBody HealthCentreRequest req) {
        return healthCentreService.update(id, req);
    }

    @GetMapping("/{id}/workers")
    public List<HealthcareWorker> getWorkers(@PathVariable Long id) { return healthCentreService.getWorkers(id); }

    @PostMapping("/{id}/workers")
    public ResponseEntity<HealthcareWorker> addWorker(@PathVariable Long id, @RequestBody HealthcareWorker worker) {
        return ResponseEntity.status(HttpStatus.CREATED).body(healthCentreService.addWorker(id, worker));
    }

    @GetMapping("/{id}/services")
    public List<CentreService> getServices(@PathVariable Long id) { return healthCentreService.getServices(id); }

    @PostMapping("/{id}/services")
    public ResponseEntity<CentreService> addService(@PathVariable Long id, @RequestBody CentreService service) {
        return ResponseEntity.status(HttpStatus.CREATED).body(healthCentreService.addService(id, service));
    }

    @GetMapping("/{id}/schedules")
    public List<OperatingSchedule> getSchedules(@PathVariable Long id) { return healthCentreService.getSchedules(id); }

    @PostMapping("/{id}/schedules")
    public ResponseEntity<OperatingSchedule> addSchedule(@PathVariable Long id, @RequestBody OperatingSchedule schedule) {
        return ResponseEntity.status(HttpStatus.CREATED).body(healthCentreService.addSchedule(id, schedule));
    }

    @GetMapping("/count-active")
    public long countActive() { return healthCentreService.countActive(); }
}
