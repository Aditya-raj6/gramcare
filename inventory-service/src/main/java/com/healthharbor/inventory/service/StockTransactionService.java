package com.healthharbor.inventory.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.inventory.entity.StockTransaction;
import com.healthharbor.inventory.repository.StockTransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class StockTransactionService extends CrudService<StockTransaction> {
    public StockTransactionService(StockTransactionRepository repository) {
        super(repository, StockTransaction.class, "Stock transaction");
    }
}
