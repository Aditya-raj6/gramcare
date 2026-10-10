package com.gramcare.sanitation.controller;

import com.gramcare.sanitation.dto.ComplaintRequest;
import com.gramcare.sanitation.entity.SanitationComplaint;
import com.gramcare.sanitation.service.SanitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sanitation/complaints")
public class ComplaintController {

    private final SanitationService sanitationService;

    public ComplaintController(SanitationService sanitationService) {
        this.sanitationService = sanitationService;
    }

    @GetMapping
    public List<SanitationComplaint> list(@RequestParam(required = false) String status) {
        return sanitationService.findComplaints(status);
    }

    @GetMapping("/{id}")
    public SanitationComplaint get(@PathVariable Long id) { return sanitationService.findComplaint(id); }

    @PostMapping
    public ResponseEntity<SanitationComplaint> create(@Valid @RequestBody ComplaintRequest req) {
        SanitationComplaint saved = sanitationService.reportComplaint(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}/status")
    public SanitationComplaint updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return sanitationService.updateComplaintStatus(id, body.get("status"), body.get("assignedWorker"), body.get("closureNotes"));
    }

    @PostMapping("/{id}/retry-action-link")
    public SanitationComplaint retryActionLink(@PathVariable Long id) {
        return sanitationService.retryActionLink(id);
    }
}
