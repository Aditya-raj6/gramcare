package com.gramcare.inventory.controller;

import com.gramcare.inventory.entity.StockTransaction;
import com.gramcare.inventory.service.MedicineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock-transactions")
public class StockTransactionController {

    private final MedicineService medicineService;

    public StockTransactionController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping("/recent")
    public List<StockTransaction> recent() {
        return medicineService.getRecentTransactions();
    }
}
