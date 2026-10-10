package com.healthharbor.inventory.service;

import com.healthharbor.inventory.entity.StockItem;
import com.healthharbor.inventory.entity.StockTransaction;
import com.healthharbor.inventory.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Writes immutable ledger entries for every stock movement. */
@Component
@RequiredArgsConstructor
public class StockLedger {

    private final StockTransactionRepository transactions;

    public StockTransaction record(StockItem item, StockTransaction.TransactionType type, int signedQuantity,
                                   String issuedTo, Long residentId, String reference, String performedBy, String notes) {
        StockTransaction t = new StockTransaction();
        t.setStockItemId(item.getId());
        t.setMedicineId(item.getMedicineId());
        t.setMedicineName(item.getMedicineName());
        t.setHealthCentreId(item.getHealthCentreId());
        t.setHealthCentreName(item.getHealthCentreName());
        t.setType(type);
        t.setQuantity(signedQuantity);
        t.setBalanceAfter(item.getQuantity());
        t.setIssuedTo(issuedTo);
        t.setResidentId(residentId);
        t.setReference(reference);
        t.setPerformedBy(performedBy);
        t.setNotes(notes);
        t.setTransactionDate(LocalDateTime.now());
        return transactions.save(t);
    }
}
