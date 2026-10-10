package com.gramcare.inventory;

import com.gramcare.inventory.dto.*;
import com.gramcare.inventory.entity.Medicine;
import com.gramcare.inventory.service.MedicineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MedicineServiceTests {

    @Autowired
    private MedicineService medicineService;

    private Medicine createMedicine(String name, int minStock) {
        MedicineRequest req = new MedicineRequest();
        req.setName(name);
        req.setMinimumStockLevel(minStock);
        return medicineService.create(req);
    }

    @Test
    void stockInThenIssueReducesQuantity() {
        Medicine m = createMedicine("Paracetamol-" + System.nanoTime(), 10);
        StockInRequest in = new StockInRequest();
        in.setBatchNumber("B1");
        in.setQuantity(100);
        medicineService.stockIn(m.getId(), in);

        StockIssueRequest issue = new StockIssueRequest();
        issue.setQuantity(40);
        issue.setReason("Dispensed at camp");
        medicineService.issueStock(m.getId(), issue);

        int remaining = medicineService.getBatches(m.getId()).stream().mapToInt(b -> b.getQuantity()).sum();
        assertEquals(60, remaining);
    }

    @Test
    void cannotIssueMoreThanAvailableStock() {
        Medicine m = createMedicine("Amoxicillin-" + System.nanoTime(), 10);
        StockInRequest in = new StockInRequest();
        in.setBatchNumber("B1");
        in.setQuantity(5);
        medicineService.stockIn(m.getId(), in);

        StockIssueRequest issue = new StockIssueRequest();
        issue.setQuantity(50);

        assertThrows(IllegalArgumentException.class, () -> medicineService.issueStock(m.getId(), issue));
    }

    @Test
    void detectsLowStock() {
        Medicine m = createMedicine("ORS-" + System.nanoTime(), 20);
        StockInRequest in = new StockInRequest();
        in.setBatchNumber("B1");
        in.setQuantity(5);
        medicineService.stockIn(m.getId(), in);

        assertTrue(medicineService.findLowStock().stream().anyMatch(med -> med.getId().equals(m.getId())));
    }

    @Test
    void issuingBelowMinimumRaisesOneAutomaticReplenishmentRequest() {
        Medicine m = createMedicine("Metformin-" + System.nanoTime(), 50);
        StockInRequest in = new StockInRequest();
        in.setBatchNumber("B1");
        in.setQuantity(60);
        medicineService.stockIn(m.getId(), in);

        StockIssueRequest issue = new StockIssueRequest();
        issue.setQuantity(20);
        medicineService.issueStock(m.getId(), issue); // 40 left: below 50
        medicineService.issueStock(m.getId(), issue); // 20 left: still below, no duplicate request

        long requests = medicineService.getReplenishmentRequests(null).stream()
                .filter(r -> r.getMedicine().getId().equals(m.getId()))
                .count();
        assertEquals(1, requests);
    }

    @Test
    void issuingFromMultipleBatchesUsesEarliestExpiryFirst() {
        Medicine m = createMedicine("Cetirizine-" + System.nanoTime(), 1);
        StockInRequest late = new StockInRequest();
        late.setBatchNumber("LATE");
        late.setQuantity(10);
        late.setExpiryDate(java.time.LocalDate.now().plusYears(2));
        medicineService.stockIn(m.getId(), late);
        StockInRequest early = new StockInRequest();
        early.setBatchNumber("EARLY");
        early.setQuantity(10);
        early.setExpiryDate(java.time.LocalDate.now().plusMonths(2));
        medicineService.stockIn(m.getId(), early);

        StockIssueRequest issue = new StockIssueRequest();
        issue.setQuantity(12);
        medicineService.issueStock(m.getId(), issue);

        var batches = medicineService.getBatches(m.getId());
        assertEquals(0, batches.stream().filter(b -> b.getBatchNumber().equals("EARLY")).findFirst().get().getQuantity());
        assertEquals(8, batches.stream().filter(b -> b.getBatchNumber().equals("LATE")).findFirst().get().getQuantity());
    }
}
