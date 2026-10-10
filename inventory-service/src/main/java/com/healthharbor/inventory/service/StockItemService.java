package com.healthharbor.inventory.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.inventory.dto.StockMovementRequest;
import com.healthharbor.inventory.entity.Medicine;
import com.healthharbor.inventory.entity.ReplenishmentRequest;
import com.healthharbor.inventory.entity.StockItem;
import com.healthharbor.inventory.entity.StockTransaction.TransactionType;
import com.healthharbor.inventory.repository.MedicineRepository;
import com.healthharbor.inventory.repository.StockItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class StockItemService extends CrudService<StockItem> {

    private final StockItemRepository stock;
    private final MedicineRepository medicines;
    private final StockLedger ledger;
    private final ReplenishmentRequestService replenishments;

    public StockItemService(StockItemRepository repository, MedicineRepository medicines, StockLedger ledger,
                            ReplenishmentRequestService replenishments) {
        super(repository, StockItem.class, "Stock item");
        this.stock = repository;
        this.medicines = medicines;
        this.ledger = ledger;
        this.replenishments = replenishments;
    }

    public List<StockItem> attentionRequired() {
        return stock.findAttentionRequired();
    }

    @Override
    protected void beforeSave(StockItem item, StockItem previous) {
        Medicine medicine = medicines.findById(item.getMedicineId())
                .orElseThrow(() -> new BusinessException("Medicine " + item.getMedicineId() + " does not exist"));
        item.setMedicineName(medicine.getName() + (medicine.getStrength() == null ? "" : " " + medicine.getStrength()));
        item.setCategory(medicine.getCategory().name());
        if (item.getReorderLevel() == null) {
            item.setReorderLevel(medicine.getDefaultReorderLevel());
        }
        if (previous == null && item.getQuantity() > 0 && item.getLastRestockedDate() == null) {
            item.setLastRestockedDate(LocalDate.now());
        }
    }

    @Override
    protected void afterCreate(StockItem item) {
        if (item.getQuantity() > 0) {
            ledger.record(item, TransactionType.RECEIPT, item.getQuantity(), null, null, "OPENING", null, "Opening stock");
        }
    }

    @Override
    protected void afterUpdate(StockItem item, StockItem previous) {
        int delta = item.getQuantity() - previous.getQuantity();
        if (delta != 0) {
            ledger.record(item, TransactionType.ADJUSTMENT, delta, null, null, "MANUAL-EDIT", null, "Stock count adjusted");
        }
        raiseReplenishmentIfLow(item, "system");
    }

    @Transactional
    public StockItem issue(Long id, StockMovementRequest request) {
        StockItem item = get(id);
        if (item.getStockStatus() == StockItem.StockStatus.EXPIRED) {
            throw new BusinessException("Batch " + Objects.toString(item.getBatchNumber(), "") + " has expired and cannot be issued");
        }
        if (request.quantity() > item.getQuantity()) {
            throw new BusinessException("Insufficient stock: only " + item.getQuantity() + " available");
        }
        item.setQuantity(item.getQuantity() - request.quantity());
        stock.save(item);
        ledger.record(item, TransactionType.ISSUE, -request.quantity(), request.issuedTo(), request.residentId(),
                request.reference(), request.performedBy(), request.notes());
        raiseReplenishmentIfLow(item, request.performedBy());
        return item;
    }

    @Transactional
    public StockItem receive(Long id, StockMovementRequest request) {
        StockItem item = get(id);
        item.setQuantity(item.getQuantity() + request.quantity());
        item.setLastRestockedDate(LocalDate.now());
        stock.save(item);
        ledger.record(item, TransactionType.RECEIPT, request.quantity(), null, null, request.reference(),
                request.performedBy(), request.notes());
        return item;
    }

    @Transactional
    public StockItem writeOffExpired(Long id, String performedBy) {
        StockItem item = get(id);
        if (item.getStockStatus() != StockItem.StockStatus.EXPIRED) {
            throw new BusinessException("Only expired batches can be written off");
        }
        int qty = item.getQuantity();
        item.setQuantity(0);
        stock.save(item);
        ledger.record(item, TransactionType.EXPIRED_WRITE_OFF, -qty, null, null, "EXPIRY", performedBy, "Expired batch written off");
        raiseReplenishmentIfLow(item, performedBy);
        return item;
    }

    private void raiseReplenishmentIfLow(StockItem item, String by) {
        StockItem.StockStatus status = item.getStockStatus();
        if (status == StockItem.StockStatus.LOW_STOCK || status == StockItem.StockStatus.OUT_OF_STOCK
                || (status == StockItem.StockStatus.EXPIRED)) {
            int target = Math.max(item.getReorderLevel() * 3 - Math.max(item.getQuantity(), 0), item.getReorderLevel());
            replenishments.raiseAutomatic(item, target,
                    status == StockItem.StockStatus.LOW_STOCK ? ReplenishmentRequest.Priority.HIGH : ReplenishmentRequest.Priority.CRITICAL, by);
        }
    }
}
