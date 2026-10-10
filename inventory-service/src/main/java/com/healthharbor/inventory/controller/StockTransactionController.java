package com.healthharbor.inventory.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.inventory.entity.StockTransaction;
import com.healthharbor.inventory.service.StockTransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-transactions")
@Tag(name = "Stock transaction")
public class StockTransactionController extends CrudController<StockTransaction> {
    public StockTransactionController(StockTransactionService service) {
        super(service);
    }
}
