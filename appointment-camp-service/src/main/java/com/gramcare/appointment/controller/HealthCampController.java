package com.gramcare.appointment.controller;

import com.gramcare.appointment.dto.HealthCampRequest;
import com.gramcare.appointment.entity.*;
import com.gramcare.appointment.service.HealthCampService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-camps")
public class HealthCampController {

    private final HealthCampService healthCampService;

    public HealthCampController(HealthCampService healthCampService) {
        this.healthCampService = healthCampService;
    }

    @GetMapping
    public List<HealthCamp> list() { return healthCampService.findAll(); }

    @GetMapping("/active")
    public List<HealthCamp> active() { return healthCampService.findActive(); }

    @GetMapping("/{id}")
    public HealthCamp get(@PathVariable Long id) { return healthCampService.findById(id); }

    @PostMapping
    public ResponseEntity<HealthCamp> create(@Valid @RequestBody HealthCampRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(healthCampService.create(req));
    }

    @PutMapping("/{id}")
    public HealthCamp updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return healthCampService.updateStatus(id, HealthCamp.CampStatus.valueOf(body.get("status").toUpperCase()));
    }

    @PostMapping("/{id}/staff")
    public ResponseEntity<CampStaff> assignStaff(@PathVariable Long id, @RequestBody CampStaff staff) {
        return ResponseEntity.status(HttpStatus.CREATED).body(healthCampService.assignStaff(id, staff));
    }

    @GetMapping("/{id}/staff")
    public List<CampStaff> staff(@PathVariable Long id) { return healthCampService.getStaff(id); }

    @PostMapping("/{id}/beneficiaries")
    public ResponseEntity<CampBeneficiary> registerBeneficiary(@PathVariable Long id, @RequestBody CampBeneficiary beneficiary) {
        return ResponseEntity.status(HttpStatus.CREATED).body(healthCampService.registerBeneficiary(id, beneficiary));
    }

    @GetMapping("/{id}/beneficiaries")
    public List<CampBeneficiary> beneficiaries(@PathVariable Long id) { return healthCampService.getBeneficiaries(id); }
}
