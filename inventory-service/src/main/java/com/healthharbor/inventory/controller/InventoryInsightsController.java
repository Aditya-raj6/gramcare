package com.healthharbor.inventory.controller;

import com.healthharbor.common.dto.PendingAction;
import com.healthharbor.inventory.entity.ReplenishmentRequest;
import com.healthharbor.inventory.entity.ReplenishmentRequest.RequestStatus;
import com.healthharbor.inventory.entity.StockItem;
import com.healthharbor.inventory.entity.StockTransaction;
import com.healthharbor.inventory.repository.MedicineRepository;
import com.healthharbor.inventory.repository.ReplenishmentRequestRepository;
import com.healthharbor.inventory.repository.StockItemRepository;
import com.healthharbor.inventory.repository.StockTransactionRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory insights")
public class InventoryInsightsController {

    private static final Set<RequestStatus> OPEN = EnumSet.of(RequestStatus.PENDING, RequestStatus.APPROVED, RequestStatus.ORDERED);

    private final MedicineRepository medicines;
    private final StockItemRepository stock;
    private final ReplenishmentRequestRepository requests;
    private final StockTransactionRepository transactions;

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        List<StockItem> all = stock.findAll();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (StockItem.StockStatus s : StockItem.StockStatus.values()) {
            byStatus.put(s.name(), all.stream().filter(i -> i.getStockStatus() == s).count());
        }
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("catalogueItems", medicines.count());
        stats.put("stockRecords", all.size());
        stats.put("lowStock", byStatus.get("LOW_STOCK"));
        stats.put("outOfStock", byStatus.get("OUT_OF_STOCK"));
        stats.put("expired", byStatus.get("EXPIRED"));
        stats.put("expiringSoon", all.stream().filter(StockItem::isExpiringSoon).count());
        stats.put("openReplenishments", requests.countByStatusIn(OPEN));
        stats.put("issuesLast30Days", transactions.countByTypeAndTransactionDateGreaterThanEqual(
                StockTransaction.TransactionType.ISSUE, LocalDateTime.now().minusDays(30)));
        stats.put("stockValue", Math.round(stock.totalValue()));
        stats.put("stockByStatus", byStatus);
        stats.put("quantityByCentre", toMap(stock.quantityByCentre()));
        stats.put("quantityByCategory", toMap(stock.quantityByCategory()));
        stats.put("replenishmentsByStatus", toMap(requests.countByStatus()));
        stats.put("topIssued", toMap(transactions.topIssued(StockTransaction.TransactionType.ISSUE,
                LocalDateTime.now().minusDays(30), PageRequest.of(0, 6))));
        return stats;
    }

    @GetMapping("/pending")
    public List<PendingAction> pending() {
        List<PendingAction> actions = new ArrayList<>();
        for (ReplenishmentRequest r : requests.findByStatusInOrderByIdDesc(OPEN)) {
            actions.add(new PendingAction("inventory", "Medicine Replenishment", r.getId(), r.getRequestNumber(),
                    r.getMedicineName() + " x" + r.getRequestedQuantity() + " for " + r.getHealthCentreName(),
                    r.getReason(), r.getHealthCentreName(), r.getPriority().name(), r.getStatus().name(),
                    r.getRequiredBy(), r.getCreatedAt(), "/api/replenishments/" + r.getId() + "/status",
                    switch (r.getStatus()) {
                        case PENDING -> List.of("APPROVED", "REJECTED");
                        case APPROVED -> List.of("ORDERED", "FULFILLED", "REJECTED");
                        default -> List.of("FULFILLED");
                    }, "/inventory"));
        }
        return actions;
    }

    private static Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        rows.forEach(r -> map.put(String.valueOf(r[0]), r[1] == null ? 0L : ((Number) r[1]).longValue()));
        return map;
    }
}
