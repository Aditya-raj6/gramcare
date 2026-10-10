package com.gramcare.appointment.controller;

import com.gramcare.appointment.entity.FollowUp;
import com.gramcare.appointment.entity.ScreeningRecord;
import com.gramcare.appointment.service.HealthCampService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final HealthCampService healthCampService;

    public BeneficiaryController(HealthCampService healthCampService) {
        this.healthCampService = healthCampService;
    }

    @PostMapping("/{id}/screenings")
    public ResponseEntity<ScreeningRecord> addScreening(@PathVariable Long id, @RequestBody ScreeningRecord screening) {
        return ResponseEntity.status(HttpStatus.CREATED).body(healthCampService.addScreening(id, screening));
    }

    @GetMapping("/{id}/screenings")
    public List<ScreeningRecord> screenings(@PathVariable Long id) { return healthCampService.getScreenings(id); }

    @PostMapping("/{id}/follow-ups")
    public ResponseEntity<FollowUp> addFollowUp(@PathVariable Long id, @RequestBody FollowUp followUp) {
        return ResponseEntity.status(HttpStatus.CREATED).body(healthCampService.addFollowUp(id, followUp));
    }

    @GetMapping("/{id}/follow-ups")
    public List<FollowUp> followUps(@PathVariable Long id) { return healthCampService.getFollowUps(id); }

    @GetMapping("/follow-ups/pending")
    public List<FollowUp> pendingFollowUps() { return healthCampService.getPendingFollowUps(); }
}
