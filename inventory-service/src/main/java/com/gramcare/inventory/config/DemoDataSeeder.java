package com.gramcare.inventory.config;

import com.gramcare.inventory.entity.*;
import com.gramcare.inventory.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Demo medicines and batches, inserted only when the medicine table is empty.
 * Two medicines start below their minimum level and one batch is expired, so
 * the low-stock and expiry highlights have something to show. The seeded
 * replenishment request is linked to the Action Tracking Service by
 * ActionLinkReconciler once that service is reachable.
 */
@Component
@Profile("!test")
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final MedicineRepository medicineRepository;
    private final InventoryBatchRepository batchRepository;
    private final StockTransactionRepository transactionRepository;
    private final ReplenishmentRequestRepository replenishmentRepository;

    public DemoDataSeeder(MedicineRepository medicineRepository, InventoryBatchRepository batchRepository,
                           StockTransactionRepository transactionRepository, ReplenishmentRequestRepository replenishmentRepository) {
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.transactionRepository = transactionRepository;
        this.replenishmentRepository = replenishmentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (medicineRepository.count() > 0) return;

        medicine("Paracetamol 500mg", "Paracetamol", "Analgesic", "tablets", 200, 1L, new int[]{350, 250}, 0);
        medicine("Amoxicillin 250mg", "Amoxicillin", "Antibiotic", "capsules", 100, 1L, new int[]{60}, 0);
        Medicine ors = medicine("ORS Sachet", "Oral rehydration salts", "Rehydration", "sachets", 150, 2L, new int[]{40}, 0);
        medicine("Metformin 500mg", "Metformin", "Antidiabetic", "tablets", 120, 2L, new int[]{300}, 0);
        medicine("Amlodipine 5mg", "Amlodipine", "Antihypertensive", "tablets", 100, 1L, new int[]{180}, 0);
        medicine("Salbutamol Inhaler", "Salbutamol", "Bronchodilator", "inhalers", 10, 3L, new int[]{14}, 0);
        medicine("Iron-Folic Acid", "Ferrous sulphate + folic acid", "Supplement", "tablets", 300, 2L, new int[]{500}, 0);
        medicine("Chlorine Tablets", "Sodium dichloroisocyanurate", "Water treatment", "tablets", 500, 3L, new int[]{220, 100}, 1);

        ReplenishmentRequest rr = new ReplenishmentRequest();
        rr.setMedicine(ors);
        rr.setRequestedQuantity(260);
        rr.setRequestedBy("Ramesh Gaikwad (Pharmacist)");
        rr.setVillage("Anandpur");
        rr.setHealthCentreId(2L);
        rr.setNotes("Diarrhoea cases rising after rains; ORS stock at Anandpur Sub Centre is low.");
        replenishmentRepository.save(rr);

        log.info("Seeded demo medicines and batches");
    }

    /** expiredBatches: how many of the batches (from the end of the array) are already expired. */
    private Medicine medicine(String name, String generic, String category, String unit, int min, Long centreId,
                               int[] batchQuantities, int expiredBatches) {
        Medicine m = new Medicine();
        m.setName(name);
        m.setGenericName(generic);
        m.setCategory(category);
        m.setUnitOfMeasure(unit);
        m.setMinimumStockLevel(min);
        m = medicineRepository.save(m);
        for (int i = 0; i < batchQuantities.length; i++) {
            boolean expired = i >= batchQuantities.length - expiredBatches;
            InventoryBatch b = new InventoryBatch();
            b.setMedicine(m);
            b.setBatchNumber(name.substring(0, 3).toUpperCase() + "-" + (2401 + i));
            b.setQuantity(batchQuantities[i]);
            b.setExpiryDate(expired ? LocalDate.now().minusDays(10) : LocalDate.now().plusMonths(6 + i * 6));
            b.setHealthCentreId(centreId);
            b = batchRepository.save(b);
            m.getBatches().add(b);

            StockTransaction tx = new StockTransaction();
            tx.setMedicine(m);
            tx.setBatch(b);
            tx.setTransactionType(StockTransaction.TransactionType.STOCK_IN);
            tx.setQuantity(batchQuantities[i]);
            tx.setReason("Opening stock");
            tx.setPerformedBy("District drug store");
            transactionRepository.save(tx);
        }
        return m;
    }
}
