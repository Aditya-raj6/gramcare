package com.gramcare.inventory.controller;

import com.gramcare.inventory.dto.*;
import com.gramcare.inventory.entity.*;
import com.gramcare.inventory.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Transactional
@RequestMapping("/api/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public List<MedicineResponse> list(@RequestParam(required = false) String name) {
        return medicineService.search(name).stream().map(MedicineResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MedicineResponse get(@PathVariable Long id) {
        return MedicineResponse.from(medicineService.findById(id));
    }

    @GetMapping("/low-stock")
    public List<MedicineResponse> lowStock() {
        return medicineService.findLowStock().stream().map(MedicineResponse::from).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<MedicineResponse> create(@Valid @RequestBody MedicineRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(MedicineResponse.from(medicineService.create(req)));
    }

    @PostMapping("/{id}/stock-in")
    public ResponseEntity<InventoryBatch> stockIn(@PathVariable Long id, @RequestBody StockInRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicineService.stockIn(id, req));
    }

    @PostMapping("/{id}/issue")
    public ResponseEntity<StockTransaction> issue(@PathVariable Long id, @RequestBody StockIssueRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicineService.issueStock(id, req));
    }

    @GetMapping("/{id}/batches")
    public List<InventoryBatch> batches(@PathVariable Long id) {
        return medicineService.getBatches(id);
    }

    @GetMapping("/{id}/transactions")
    public List<StockTransaction> transactions(@PathVariable Long id) {
        return medicineService.getTransactionHistory(id);
    }
}
