package com.gramcare.sanitation.controller;

import com.gramcare.sanitation.dto.FacilityRequest;
import com.gramcare.sanitation.entity.CleanlinessInspection;
import com.gramcare.sanitation.entity.SanitationFacility;
import com.gramcare.sanitation.service.SanitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sanitation/facilities")
public class FacilityController {

    private final SanitationService sanitationService;

    public FacilityController(SanitationService sanitationService) {
        this.sanitationService = sanitationService;
    }

    @GetMapping
    public List<SanitationFacility> list() { return sanitationService.findAllFacilities(); }

    @GetMapping("/{id}")
    public SanitationFacility get(@PathVariable Long id) { return sanitationService.findFacility(id); }

    @PostMapping
    public ResponseEntity<SanitationFacility> create(@Valid @RequestBody FacilityRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sanitationService.createFacility(req));
    }

    @PostMapping("/{id}/inspections")
    public ResponseEntity<CleanlinessInspection> addInspection(@PathVariable Long id, @RequestBody CleanlinessInspection inspection) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sanitationService.addInspection(id, inspection));
    }

    @GetMapping("/{id}/inspections")
    public List<CleanlinessInspection> inspections(@PathVariable Long id) { return sanitationService.getInspections(id); }
}
