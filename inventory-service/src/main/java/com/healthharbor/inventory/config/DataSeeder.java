package com.healthharbor.inventory.config;

import com.healthharbor.inventory.entity.*;
import com.healthharbor.inventory.entity.Medicine.DosageForm;
import com.healthharbor.inventory.entity.Medicine.ItemCategory;
import com.healthharbor.inventory.repository.*;
import com.healthharbor.inventory.service.StockLedger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "healthharbor.seed-data", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final String[] CENTRES = {null, "PHC Rampur", "Sub-Centre Sundarpur", "CHC Kishanganj", "Sub-Centre Devgarh"};

    private final MedicineRepository medicines;
    private final StockItemRepository stock;
    private final ReplenishmentRequestRepository requests;
    private final StockLedger ledger;

    @Override
    @Transactional
    public void run(String... args) {
        if (medicines.count() > 0) {
            return;
        }
        Medicine pcm = med("Paracetamol", "Acetaminophen", ItemCategory.MEDICINE, DosageForm.TABLET, "500mg", "strip of 10", 100);
        Medicine ors = med("ORS", "Oral Rehydration Salts", ItemCategory.MEDICINE, DosageForm.SACHET, "20.5g", "sachet", 200);
        Medicine zinc = med("Zinc Sulphate", "Zinc", ItemCategory.MEDICINE, DosageForm.TABLET, "20mg", "strip of 10", 80);
        Medicine metformin = med("Metformin", "Metformin HCl", ItemCategory.MEDICINE, DosageForm.TABLET, "500mg", "strip of 10", 100);
        Medicine amlo = med("Amlodipine", "Amlodipine besylate", ItemCategory.MEDICINE, DosageForm.TABLET, "5mg", "strip of 10", 80);
        Medicine amox = med("Amoxicillin", "Amoxicillin trihydrate", ItemCategory.MEDICINE, DosageForm.CAPSULE, "500mg", "strip of 10", 60);
        Medicine ifa = med("Iron Folic Acid", "Ferrous sulphate + Folic acid", ItemCategory.MEDICINE, DosageForm.TABLET, "100mg/0.5mg", "strip of 10", 150);
        Medicine albendazole = med("Albendazole", "Albendazole", ItemCategory.MEDICINE, DosageForm.TABLET, "400mg", "tablet", 100);
        Medicine bcg = med("BCG Vaccine", "Bacillus Calmette-Guerin", ItemCategory.VACCINE, DosageForm.VIAL, "10 doses", "vial", 10);
        Medicine penta = med("Pentavalent Vaccine", "DPT-HepB-Hib", ItemCategory.VACCINE, DosageForm.VIAL, "1 dose", "vial", 20);
        Medicine gloves = med("Surgical Gloves", null, ItemCategory.CONSUMABLE, DosageForm.UNIT, "Size 7", "pair", 200);
        Medicine syringe = med("Disposable Syringe", null, ItemCategory.SUPPLY, DosageForm.UNIT, "5ml", "piece", 300);
        Medicine rdt = med("Malaria RDT Kit", null, ItemCategory.DIAGNOSTIC_KIT, DosageForm.KIT, "Pf/Pv", "kit", 50);
        Medicine chlorine = med("Chlorine Tablets", "Sodium dichloroisocyanurate", ItemCategory.SUPPLY, DosageForm.TABLET, "67mg", "strip of 10", 200);

        LocalDate t = LocalDate.now();
        StockItem s1 = item(pcm, 1, "PCM-2401", 640, t.plusMonths(18), 1.2);
        item(ors, 1, "ORS-2405", 420, t.plusMonths(20), 4.5);
        item(metformin, 1, "MET-2311", 85, t.plusMonths(10), 1.8);
        item(amlo, 1, "AML-2402", 300, t.plusMonths(14), 1.5);
        item(ifa, 1, "IFA-2403", 900, t.plusMonths(12), 0.6);
        item(bcg, 1, "BCG-2406", 14, t.plusMonths(6), 85.0);
        item(penta, 1, "PEN-2404", 32, t.plusDays(40), 120.0);
        item(syringe, 1, "SYR-2401", 1200, t.plusYears(3), 3.0);
        item(rdt, 1, "RDT-2402", 75, t.plusMonths(9), 25.0);
        item(pcm, 2, "PCM-2310", 60, t.plusMonths(8), 1.2);
        item(ifa, 2, "IFA-2312", 420, t.plusMonths(11), 0.6);
        item(ors, 2, "ORS-2311", 0, t.plusMonths(15), 4.5);
        item(gloves, 2, "GLV-2401", 140, t.plusYears(2), 6.0);
        item(chlorine, 2, "CLT-2402", 500, t.plusYears(2), 0.8);
        item(pcm, 3, "PCM-2402", 1500, t.plusMonths(22), 1.2);
        item(amox, 3, "AMX-2401", 45, t.plusMonths(7), 3.5);
        StockItem s17 = item(ors, 3, "ORS-2402", 650, t.plusMonths(19), 4.5);
        item(zinc, 3, "ZNC-2403", 380, t.plusMonths(16), 1.0);
        item(albendazole, 3, "ALB-2309", 120, t.minusDays(10), 2.0);
        item(penta, 3, "PEN-2403", 60, t.plusMonths(5), 120.0);
        item(metformin, 3, "MET-2402", 520, t.plusMonths(13), 1.8);
        item(pcm, 4, "PCM-2312", 30, t.plusMonths(6), 1.2);
        item(ors, 4, "ORS-2312", 40, t.plusMonths(12), 4.5);
        item(chlorine, 4, "CLT-2311", 0, t.plusYears(1), 0.8);
        item(amox, 4, "AMX-2312", 25, t.plusDays(25), 3.5);

        issue(s1, 40, "Ramesh Kumar", 1L, "APT-00001");
        issue(s17, 10, "Anil Verma", 5L, "APT-00002");
        issue(s1, 120, "School health programme", null, "SHP-RAMPUR");

        request(ors, 2, 400, ReplenishmentRequest.Priority.CRITICAL, ReplenishmentRequest.RequestStatus.PENDING,
                ReplenishmentRequest.RequestSource.AUTO_LOW_STOCK, "Auto-raised: stock out of stock (0 left)", "system", 0);
        request(metformin, 1, 300, ReplenishmentRequest.Priority.HIGH, ReplenishmentRequest.RequestStatus.APPROVED,
                ReplenishmentRequest.RequestSource.AUTO_LOW_STOCK, "Auto-raised: low stock (85 left)", "system", 85);
        request(chlorine, 4, 600, ReplenishmentRequest.Priority.CRITICAL, ReplenishmentRequest.RequestStatus.PENDING,
                ReplenishmentRequest.RequestSource.MANUAL, "Needed for well chlorination drive after contamination report", "Prakash Yadav", 0);
        request(amox, 3, 200, ReplenishmentRequest.Priority.MEDIUM, ReplenishmentRequest.RequestStatus.ORDERED,
                ReplenishmentRequest.RequestSource.MANUAL, "Monsoon season buffer", "Ravi Kumar", 45);
        request(ifa, 2, 500, ReplenishmentRequest.Priority.LOW, ReplenishmentRequest.RequestStatus.FULFILLED,
                ReplenishmentRequest.RequestSource.MANUAL, "Anaemia Mukt Bharat campaign", "Kavita Rao", 120);
        log.info("Seeded {} catalogue items, {} stock records, {} replenishment requests", medicines.count(), stock.count(), requests.count());
    }

    private Medicine med(String name, String generic, ItemCategory category, DosageForm form, String strength, String unit, int reorder) {
        Medicine m = new Medicine();
        m.setName(name);
        m.setGenericName(generic);
        m.setCategory(category);
        m.setForm(form);
        m.setStrength(strength);
        m.setUnit(unit);
        m.setManufacturer(category == ItemCategory.VACCINE ? "Serum Institute of India" : "Govt. Medical Supplies Corp.");
        m.setDefaultReorderLevel(reorder);
        m.setEssential(true);
        m = medicines.save(m);
        m.setCode(String.format("MED-%05d", m.getId()));
        return medicines.save(m);
    }

    private StockItem item(Medicine m, int centreId, String batch, int qty, LocalDate expiry, double price) {
        StockItem s = new StockItem();
        s.setMedicineId(m.getId());
        s.setMedicineName(m.getName() + " " + m.getStrength());
        s.setCategory(m.getCategory().name());
        s.setHealthCentreId((long) centreId);
        s.setHealthCentreName(CENTRES[centreId]);
        s.setBatchNumber(batch);
        s.setQuantity(qty);
        s.setReorderLevel(m.getDefaultReorderLevel());
        s.setExpiryDate(expiry);
        s.setUnitPrice(price);
        s.setLastRestockedDate(LocalDate.now().minusDays(Math.abs(batch.hashCode()) % 60));
        s.setStorageLocation(m.getCategory() == ItemCategory.VACCINE ? "ILR / Cold chain" : "Store room rack " + (char) ('A' + centreId));
        s = stock.save(s);
        if (qty > 0) {
            ledger.record(s, StockTransaction.TransactionType.RECEIPT, qty, null, null, "OPENING", "system", "Opening stock");
        }
        return s;
    }

    private void issue(StockItem s, int qty, String to, Long residentId, String ref) {
        s.setQuantity(s.getQuantity() - qty);
        stock.save(s);
        ledger.record(s, StockTransaction.TransactionType.ISSUE, -qty, to, residentId, ref, "Ravi Kumar", null);
    }

    private void request(Medicine m, int centreId, int qty, ReplenishmentRequest.Priority priority, ReplenishmentRequest.RequestStatus status,
                         ReplenishmentRequest.RequestSource source, String reason, String by, int current) {
        ReplenishmentRequest r = new ReplenishmentRequest();
        r.setMedicineId(m.getId());
        r.setMedicineName(m.getName() + " " + m.getStrength());
        r.setHealthCentreId((long) centreId);
        r.setHealthCentreName(CENTRES[centreId]);
        r.setRequestedQuantity(qty);
        r.setCurrentStock(current);
        r.setPriority(priority);
        r.setStatus(status);
        r.setSource(source);
        r.setReason(reason);
        r.setRequestedBy(by);
        r.setRequiredBy(LocalDate.now().plusDays(priority == ReplenishmentRequest.Priority.CRITICAL ? 2 : 10));
        if (status != ReplenishmentRequest.RequestStatus.PENDING) {
            r.setApprovedBy("Dr. Meera Iyer");
        }
        if (status == ReplenishmentRequest.RequestStatus.FULFILLED) {
            r.setFulfilledDate(LocalDate.now().minusDays(3));
        }
        r = requests.save(r);
        r.setRequestNumber(String.format("REQ-%05d", r.getId()));
        requests.save(r);
    }
}
