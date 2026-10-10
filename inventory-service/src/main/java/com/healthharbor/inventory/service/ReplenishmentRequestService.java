package com.healthharbor.inventory.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.common.dto.StatusUpdateRequest;
import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.inventory.entity.Medicine;
import com.healthharbor.inventory.entity.ReplenishmentRequest;
import com.healthharbor.inventory.entity.ReplenishmentRequest.RequestStatus;
import com.healthharbor.inventory.entity.StockItem;
import com.healthharbor.inventory.entity.StockTransaction.TransactionType;
import com.healthharbor.inventory.repository.MedicineRepository;
import com.healthharbor.inventory.repository.ReplenishmentRequestRepository;
import com.healthharbor.inventory.repository.StockItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Replenishment workflow: PENDING → APPROVED → ORDERED → FULFILLED (or REJECTED).
 * Fulfilment automatically receives the quantity into the centre's stock.
 */
@Service
public class ReplenishmentRequestService extends CrudService<ReplenishmentRequest> {

    static final Set<RequestStatus> OPEN = EnumSet.of(RequestStatus.PENDING, RequestStatus.APPROVED, RequestStatus.ORDERED);

    private static final Map<RequestStatus, Set<RequestStatus>> TRANSITIONS = Map.of(
            RequestStatus.PENDING, EnumSet.of(RequestStatus.APPROVED, RequestStatus.REJECTED),
            RequestStatus.APPROVED, EnumSet.of(RequestStatus.ORDERED, RequestStatus.FULFILLED, RequestStatus.REJECTED),
            RequestStatus.ORDERED, EnumSet.of(RequestStatus.FULFILLED),
            RequestStatus.FULFILLED, EnumSet.noneOf(RequestStatus.class),
            RequestStatus.REJECTED, EnumSet.of(RequestStatus.PENDING));

    private final ReplenishmentRequestRepository requests;
    private final MedicineRepository medicines;
    private final StockItemRepository stock;
    private final StockLedger ledger;

    public ReplenishmentRequestService(ReplenishmentRequestRepository repository, MedicineRepository medicines,
                                       StockItemRepository stock, StockLedger ledger) {
        super(repository, ReplenishmentRequest.class, "Replenishment request");
        this.requests = repository;
        this.medicines = medicines;
        this.stock = stock;
        this.ledger = ledger;
    }

    @Override
    protected void beforeSave(ReplenishmentRequest r, ReplenishmentRequest previous) {
        if (previous != null) {
            return;
        }
        Medicine medicine = medicines.findById(r.getMedicineId())
                .orElseThrow(() -> new BusinessException("Medicine " + r.getMedicineId() + " does not exist"));
        r.setMedicineName(medicine.getName() + (medicine.getStrength() == null ? "" : " " + medicine.getStrength()));
        List<StockItem> batches = stock.findByMedicineIdAndHealthCentreIdOrderByExpiryDateAsc(r.getMedicineId(), r.getHealthCentreId());
        r.setCurrentStock(batches.stream().mapToInt(StockItem::getQuantity).sum());
        if ((r.getHealthCentreName() == null || r.getHealthCentreName().isBlank()) && !batches.isEmpty()) {
            r.setHealthCentreName(batches.get(0).getHealthCentreName());
        }
        r.setStatus(RequestStatus.PENDING);
    }

    @Override
    protected void afterCreate(ReplenishmentRequest r) {
        r.setRequestNumber(reference("REQ", r.getId()));
        repository.save(r);
    }

    @Override
    protected void validateTransition(ReplenishmentRequest r, Object oldStatus, Object newStatus) {
        if (oldStatus != newStatus && !TRANSITIONS.get((RequestStatus) oldStatus).contains((RequestStatus) newStatus)) {
            throw new BusinessException("Cannot move request from " + oldStatus + " to " + newStatus);
        }
    }

    @Override
    protected void onStatusChange(ReplenishmentRequest r, Object oldStatus, Object newStatus, StatusUpdateRequest req) {
        if (req.notes() != null && !req.notes().isBlank()) {
            r.setNotes(req.notes());
        }
        if (newStatus == RequestStatus.APPROVED) {
            r.setApprovedBy(req.updatedBy());
        }
        if (newStatus == RequestStatus.FULFILLED && oldStatus != RequestStatus.FULFILLED) {
            fulfil(r, req.updatedBy());
        }
    }

    private void fulfil(ReplenishmentRequest r, String by) {
        StockItem target = stock.findByMedicineIdAndHealthCentreIdOrderByExpiryDateAsc(r.getMedicineId(), r.getHealthCentreId())
                .stream().filter(s -> s.getStockStatus() != StockItem.StockStatus.EXPIRED).findFirst()
                .orElseGet(() -> newBatch(r));
        target.setQuantity(target.getQuantity() + r.getRequestedQuantity());
        target.setLastRestockedDate(LocalDate.now());
        stock.save(target);
        ledger.record(target, TransactionType.RECEIPT, r.getRequestedQuantity(), null, null, r.getRequestNumber(), by,
                "Replenishment fulfilled");
        r.setFulfilledDate(LocalDate.now());
    }

    private StockItem newBatch(ReplenishmentRequest r) {
        Medicine medicine = medicines.findById(r.getMedicineId()).orElseThrow();
        StockItem item = new StockItem();
        item.setMedicineId(r.getMedicineId());
        item.setMedicineName(r.getMedicineName());
        item.setCategory(medicine.getCategory().name());
        item.setHealthCentreId(r.getHealthCentreId());
        item.setHealthCentreName(r.getHealthCentreName() == null ? "Centre " + r.getHealthCentreId() : r.getHealthCentreName());
        item.setBatchNumber("RPL-" + r.getId());
        item.setQuantity(0);
        item.setReorderLevel(medicine.getDefaultReorderLevel());
        item.setExpiryDate(LocalDate.now().plusYears(2));
        return stock.save(item);
    }

    /** Creates a system-generated request unless one is already open for this item at this centre. */
    void raiseAutomatic(StockItem item, int quantity, ReplenishmentRequest.Priority priority, String by) {
        if (requests.existsByMedicineIdAndHealthCentreIdAndStatusIn(item.getMedicineId(), item.getHealthCentreId(), OPEN)) {
            return;
        }
        ReplenishmentRequest r = new ReplenishmentRequest();
        r.setMedicineId(item.getMedicineId());
        r.setHealthCentreId(item.getHealthCentreId());
        r.setHealthCentreName(item.getHealthCentreName());
        r.setRequestedQuantity(quantity);
        r.setPriority(priority);
        r.setSource(ReplenishmentRequest.RequestSource.AUTO_LOW_STOCK);
        r.setRequestedBy(by == null ? "system" : by);
        r.setReason("Auto-raised: stock " + item.getStockStatus().name().replace('_', ' ').toLowerCase()
                + " (" + item.getQuantity() + " left, reorder level " + item.getReorderLevel() + ")");
        r.setRequiredBy(LocalDate.now().plusDays(priority == ReplenishmentRequest.Priority.CRITICAL ? 2 : 7));
        create(r);
    }
}
