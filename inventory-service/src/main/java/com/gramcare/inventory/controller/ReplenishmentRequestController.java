package com.gramcare.inventory.controller;

import com.gramcare.inventory.dto.ReplenishmentRequestDto;
import com.gramcare.inventory.entity.ReplenishmentRequest;
import com.gramcare.inventory.service.MedicineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/replenishment-requests")
public class ReplenishmentRequestController {

    private final MedicineService medicineService;

    public ReplenishmentRequestController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public List<ReplenishmentRequest> list(@RequestParam(required = false) String status) {
        return medicineService.getReplenishmentRequests(status);
    }

    @PostMapping
    public ResponseEntity<ReplenishmentRequest> create(@RequestBody ReplenishmentRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicineService.createReplenishmentRequest(dto));
    }

    @PostMapping("/{id}/retry-action-link")
    public ReplenishmentRequest retryActionLink(@PathVariable Long id) {
        return medicineService.retryActionLink(id);
    }

    @PutMapping("/{id}/status")
    public ReplenishmentRequest updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        return medicineService.updateReplenishmentStatus(id, body.get("status"));
    }
}
