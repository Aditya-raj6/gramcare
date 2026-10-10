package com.healthharbor.inventory;

import com.healthharbor.common.dto.StatusUpdateRequest;
import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.inventory.dto.StockMovementRequest;
import com.healthharbor.inventory.entity.Medicine;
import com.healthharbor.inventory.entity.ReplenishmentRequest;
import com.healthharbor.inventory.entity.StockItem;
import com.healthharbor.inventory.repository.ReplenishmentRequestRepository;
import com.healthharbor.inventory.service.MedicineService;
import com.healthharbor.inventory.service.ReplenishmentRequestService;
import com.healthharbor.inventory.service.StockItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Issue → auto-replenishment → approval → fulfilment, against an in-memory database. */
@SpringBootTest
@ActiveProfiles("test")
class StockWorkflowIntegrationTest {

    @Autowired MedicineService medicines;
    @Autowired StockItemService stock;
    @Autowired ReplenishmentRequestService replenishments;
    @Autowired ReplenishmentRequestRepository requestRepository;

    @Test
    void issuingBelowReorderLevelRaisesAndFulfilsReplenishment() {
        Medicine m = new Medicine();
        m.setName("ORS");
        m.setStrength("20.5g");
        m.setCategory(Medicine.ItemCategory.MEDICINE);
        m.setDefaultReorderLevel(50);
        m = medicines.create(m);
        assertThat(m.getCode()).startsWith("MED-");

        StockItem item = new StockItem();
        item.setMedicineId(m.getId());
        item.setHealthCentreId(2L);
        item.setHealthCentreName("Sub-Centre Sundarpur");
        item.setQuantity(80);
        item.setExpiryDate(LocalDate.now().plusYears(1));
        item = stock.create(item);
        assertThat(item.getStockStatus()).isEqualTo(StockItem.StockStatus.IN_STOCK);

        assertThatThrownBy(() -> stock.issue(stock.list(null).get(0).getId(), new StockMovementRequest(500, "x", null, null, null, null)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Insufficient stock");

        item = stock.issue(item.getId(), new StockMovementRequest(40, "Mohan Lal", null, "APT-1", "tester", null));
        assertThat(item.getQuantity()).isEqualTo(40);
        assertThat(item.getStockStatus()).isEqualTo(StockItem.StockStatus.LOW_STOCK);

        List<ReplenishmentRequest> raised = requestRepository.findAll();
        assertThat(raised).hasSize(1);
        ReplenishmentRequest request = raised.get(0);
        assertThat(request.getSource()).isEqualTo(ReplenishmentRequest.RequestSource.AUTO_LOW_STOCK);
        assertThat(request.getStatus()).isEqualTo(ReplenishmentRequest.RequestStatus.PENDING);

        // A second low issue must not raise a duplicate while one is open
        stock.issue(item.getId(), new StockMovementRequest(5, "Sita Devi", null, null, "tester", null));
        assertThat(requestRepository.count()).isEqualTo(1);

        assertThatThrownBy(() -> replenishments.updateStatus(request.getId(), new StatusUpdateRequest("FULFILLED", null, null, "officer")))
                .isInstanceOf(BusinessException.class);
        replenishments.updateStatus(request.getId(), new StatusUpdateRequest("APPROVED", null, null, "officer"));
        replenishments.updateStatus(request.getId(), new StatusUpdateRequest("FULFILLED", "Received from district store", null, "officer"));

        StockItem after = stock.get(item.getId());
        assertThat(after.getQuantity()).isEqualTo(35 + request.getRequestedQuantity());
        assertThat(replenishments.get(request.getId()).getFulfilledDate()).isEqualTo(LocalDate.now());
    }
}
