package com.gramcare.resident.controller;

import com.gramcare.resident.dto.MedicalRecordRequest;
import com.gramcare.resident.dto.ResidentRequest;
import com.gramcare.resident.dto.ResidentResponse;
import com.gramcare.resident.entity.MedicalRecord;
import com.gramcare.resident.entity.Resident;
import com.gramcare.resident.service.ResidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Transactional
@RequestMapping("/api/residents")
public class ResidentController {

    private final ResidentService residentService;

    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    @GetMapping
    public List<ResidentResponse> list(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) String village) {
        List<Resident> residents = (name != null || village != null)
                ? residentService.search(name, village)
                : residentService.findAll();
        return residents.stream().map(ResidentResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResidentResponse get(@PathVariable Long id) {
        return ResidentResponse.from(residentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ResidentResponse> create(@Valid @RequestBody ResidentRequest req) {
        Resident saved = residentService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResidentResponse.from(saved));
    }

    @PutMapping("/{id}")
    public ResidentResponse update(@PathVariable Long id, @Valid @RequestBody ResidentRequest req) {
        return ResidentResponse.from(residentService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        residentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/visits")
    public List<MedicalRecord> getVisits(@PathVariable Long id) {
        return residentService.getVisitHistory(id);
    }

    @PostMapping("/{id}/visits")
    public ResponseEntity<MedicalRecord> addVisit(@PathVariable Long id, @RequestBody MedicalRecordRequest req) {
        MedicalRecord saved = residentService.addVisit(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/count")
    public long count() {
        return residentService.countAll();
    }
}
