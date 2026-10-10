package com.gramcare.inventory.service;

import com.gramcare.inventory.dto.*;
import com.gramcare.inventory.entity.*;
import com.gramcare.inventory.exception.ResourceNotFoundException;
import com.gramcare.inventory.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final InventoryBatchRepository batchRepository;
    private final StockTransactionRepository transactionRepository;
    private final ReplenishmentRequestRepository replenishmentRepository;
    private final ActionServiceClient actionServiceClient;

    public MedicineService(MedicineRepository medicineRepository,
                            InventoryBatchRepository batchRepository,
                            StockTransactionRepository transactionRepository,
                            ReplenishmentRequestRepository replenishmentRepository,
                            ActionServiceClient actionServiceClient) {
        this.actionServiceClient = actionServiceClient;
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.transactionRepository = transactionRepository;
        this.replenishmentRepository = replenishmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Medicine> findAll() { return medicineRepository.findAll(); }

    @Transactional(readOnly = true)
    public Medicine findById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<Medicine> search(String name) {
        return name == null || name.isBlank() ? medicineRepository.findAll()
                : medicineRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<InventoryBatch> getBatches(Long medicineId) {
        findById(medicineId);
        return batchRepository.findByMedicineIdOrderByExpiryDateAsc(medicineId);
    }

    @Transactional(readOnly = true)
    public List<Medicine> findLowStock() {
        return medicineRepository.findAll().stream()
                .filter(Medicine::isLowStock)
                .collect(Collectors.toList());
    }

    public Medicine create(MedicineRequest req) {
        if (medicineRepository.findByNameIgnoreCase(req.getName()).isPresent()) {
            throw new IllegalArgumentException("A medicine named '" + req.getName() + "' already exists");
        }
        Medicine m = new Medicine();
        m.setName(req.getName());
        m.setGenericName(req.getGenericName());
        m.setCategory(req.getCategory());
        if (req.getUnitOfMeasure() != null) m.setUnitOfMeasure(req.getUnitOfMeasure());
        m.setMinimumStockLevel(req.getMinimumStockLevel());
        return medicineRepository.save(m);
    }

    public InventoryBatch stockIn(Long medicineId, StockInRequest req) {
        if (req.getQuantity() <= 0) {
            throw new IllegalArgumentException("Stock-in quantity must be positive");
        }
        Medicine medicine = findById(medicineId);
        InventoryBatch batch = new InventoryBatch();
        batch.setMedicine(medicine);
        batch.setBatchNumber(req.getBatchNumber());
        batch.setQuantity(req.getQuantity());
        batch.setExpiryDate(req.getExpiryDate());
        batch.setHealthCentreId(req.getHealthCentreId());
        if (req.getBatchNumber() == null || req.getBatchNumber().isBlank()) {
            throw new IllegalArgumentException("Batch number is required");
        }
        InventoryBatch saved = batchRepository.save(batch);
        medicine.getBatches().add(saved);

        StockTransaction tx = new StockTransaction();
        tx.setMedicine(medicine);
        tx.setBatch(saved);
        tx.setTransactionType(StockTransaction.TransactionType.STOCK_IN);
        tx.setQuantity(req.getQuantity());
        tx.setPerformedBy(req.getPerformedBy());
        tx.setReason("Stock received");
        transactionRepository.save(tx);

        return saved;
    }

    /**
     * Issues medicine stock, deducting from the batch(es) with the earliest expiry
     * date first. Throws IllegalArgumentException if the requested quantity exceeds
     * the total available stock, so stock can never go negative.
     */
    public StockTransaction issueStock(Long medicineId, StockIssueRequest req) {
        if (req.getQuantity() <= 0) {
            throw new IllegalArgumentException("Issue quantity must be positive");
        }
        Medicine medicine = findById(medicineId);
        // Only unexpired batches can be issued; earliest expiry goes out first.
        List<InventoryBatch> batches = batchRepository.findByMedicineIdOrderByExpiryDateAsc(medicineId)
                .stream().filter(b -> !b.isExpired()).toList();

        int available = batches.stream().mapToInt(InventoryBatch::getQuantity).sum();
        if (req.getQuantity() > available) {
            throw new IllegalArgumentException(
                    "Cannot issue " + req.getQuantity() + " units of " + medicine.getName()
                            + "; only " + available + " unexpired units are in stock");
        }

        int remaining = req.getQuantity();
        for (InventoryBatch batch : batches) {
            if (remaining <= 0) break;
            int deduct = Math.min(batch.getQuantity(), remaining);
            batch.setQuantity(batch.getQuantity() - deduct);
            remaining -= deduct;
            batchRepository.save(batch);
        }

        StockTransaction tx = new StockTransaction();
        tx.setMedicine(medicine);
        tx.setTransactionType(StockTransaction.TransactionType.STOCK_ISSUE);
        tx.setQuantity(req.getQuantity());
        tx.setReason(req.getReason());
        tx.setPerformedBy(req.getPerformedBy());
        StockTransaction savedTx = transactionRepository.save(tx);

        raiseLowStockRequestIfNeeded(medicine);
        return savedTx;
    }

    /**
     * When stock drops below the minimum and nothing is already pending or
     * approved for this medicine, raise a replenishment request automatically
     * (which in turn creates a MEDICINE_STOCK action item).
     */
    private void raiseLowStockRequestIfNeeded(Medicine medicine) {
        int current = batchRepository.findByMedicineIdOrderByExpiryDateAsc(medicine.getId())
                .stream().filter(b -> !b.isExpired()).mapToInt(InventoryBatch::getQuantity).sum();
        if (current >= medicine.getMinimumStockLevel()) return;
        boolean alreadyRequested = replenishmentRepository.existsByMedicineIdAndStatusIn(medicine.getId(),
                List.of(ReplenishmentRequest.RequestStatus.PENDING, ReplenishmentRequest.RequestStatus.APPROVED));
        if (alreadyRequested) return;

        ReplenishmentRequest rr = new ReplenishmentRequest();
        rr.setMedicine(medicine);
        rr.setRequestedQuantity(Math.max(medicine.getMinimumStockLevel() * 2 - current, 1));
        rr.setRequestedBy("System (low-stock alert)");
        rr.setNotes("Stock fell to " + current + " " + medicine.getUnitOfMeasure()
                + ", below the minimum of " + medicine.getMinimumStockLevel() + ".");
        rr.setAutoGenerated(true);
        ReplenishmentRequest saved = replenishmentRepository.save(rr);
        linkToActionService(saved, current == 0 ? "CRITICAL" : "HIGH");
    }

    @Transactional(readOnly = true)
    public List<StockTransaction> getTransactionHistory(Long medicineId) {
        return transactionRepository.findByMedicineIdOrderByTransactionDateDesc(medicineId);
    }

    @Transactional(readOnly = true)
    public List<StockTransaction> getRecentTransactions() {
        return transactionRepository.findTop20ByOrderByTransactionDateDesc();
    }

    public ReplenishmentRequest createReplenishmentRequest(ReplenishmentRequestDto dto) {
        Medicine medicine = findById(dto.getMedicineId());
        ReplenishmentRequest rr = new ReplenishmentRequest();
        rr.setMedicine(medicine);
        rr.setRequestedQuantity(dto.getRequestedQuantity());
        rr.setRequestedBy(dto.getRequestedBy());
        rr.setNotes(dto.getNotes());
        rr.setVillage(dto.getVillage());
        rr.setHealthCentreId(dto.getHealthCentreId());
        if (dto.getRequestedQuantity() <= 0) {
            throw new IllegalArgumentException("Requested quantity must be positive");
        }
        ReplenishmentRequest saved = replenishmentRepository.save(rr);
        String priority = dto.getPriority() != null && !dto.getPriority().isBlank()
                ? dto.getPriority().toUpperCase()
                : (medicine.isLowStock() ? "HIGH" : "MEDIUM");
        linkToActionService(saved, priority);
        return saved;
    }

    /**
     * Creates the linked MEDICINE_STOCK action item. Safe to call repeatedly:
     * the Action Tracking Service de-duplicates on (service, type, id), so a
     * retry returns the existing item instead of creating a second one.
     */
    private void linkToActionService(ReplenishmentRequest rr, String priority) {
        if (rr.getActionItemId() != null) return;
        Medicine m = rr.getMedicine();
        ActionItemRequest req = new ActionItemRequest();
        req.setTitle("Medicine shortage: " + m.getName());
        req.setDescription("Replenishment of " + rr.getRequestedQuantity() + " " + m.getUnitOfMeasure()
                + " requested by " + (rr.getRequestedBy() != null ? rr.getRequestedBy() : "unknown")
                + ". Current stock " + m.getTotalQuantity() + ", minimum " + m.getMinimumStockLevel() + "."
                + (rr.getNotes() != null ? " " + rr.getNotes() : ""));
        req.setCategory("MEDICINE_STOCK");
        req.setVillage(rr.getVillage());
        req.setHealthCentreId(rr.getHealthCentreId());
        req.setPriority(priority);
        req.setReportedBy(rr.getRequestedBy());
        req.setSourceService("inventory-service");
        req.setSourceReferenceType("REPLENISHMENT_REQUEST");
        req.setSourceReferenceId(rr.getId());
        actionServiceClient.createActionItem(req).ifPresent(resp -> {
            rr.setActionItemId(resp.getId());
            replenishmentRepository.save(rr);
        });
    }

    public ReplenishmentRequest retryActionLink(Long id) {
        ReplenishmentRequest rr = replenishmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Replenishment request not found with id " + id));
        linkToActionService(rr, rr.getMedicine().isLowStock() ? "HIGH" : "MEDIUM");
        return rr;
    }

    /** Links any requests created while the Action Tracking Service was down. Returns how many are still unlinked. */
    public int reconcileUnlinked() {
        List<ReplenishmentRequest> unlinked = replenishmentRepository.findByActionItemIdIsNull();
        for (ReplenishmentRequest rr : unlinked) {
            linkToActionService(rr, rr.getMedicine().isLowStock() ? "HIGH" : "MEDIUM");
        }
        return (int) unlinked.stream().filter(r -> r.getActionItemId() == null).count();
    }

    @Transactional(readOnly = true)
    public List<ReplenishmentRequest> getReplenishmentRequests(String status) {
        if (status == null || status.isBlank()) return replenishmentRepository.findAll();
        return replenishmentRepository.findByStatus(ReplenishmentRequest.RequestStatus.valueOf(status.toUpperCase()));
    }

    public ReplenishmentRequest updateReplenishmentStatus(Long id, String status) {
        ReplenishmentRequest rr = replenishmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Replenishment request not found with id " + id));
        rr.setStatus(ReplenishmentRequest.RequestStatus.valueOf(status.toUpperCase()));
        if (rr.getStatus() == ReplenishmentRequest.RequestStatus.FULFILLED
                || rr.getStatus() == ReplenishmentRequest.RequestStatus.REJECTED) {
            rr.setResolvedAt(LocalDateTime.now());
        }
        return replenishmentRepository.save(rr);
    }
}
