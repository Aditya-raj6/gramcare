package com.healthharbor.inventory.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.inventory.dto.StockMovementRequest;
import com.healthharbor.inventory.entity.StockItem;
import com.healthharbor.inventory.service.StockItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@Tag(name = "Stock item")
public class StockItemController extends CrudController<StockItem> {

    private final StockItemService stockService;

    public StockItemController(StockItemService service) {
        super(service);
        this.stockService = service;
    }

    @GetMapping("/attention")
    @Operation(summary = "Low, out-of-stock and expired batches")
    public List<StockItem> attention() {
        return stockService.attentionRequired();
    }

    @PostMapping("/{id}/issue")
    @Operation(summary = "Issue stock to a resident, camp or ward")
    public StockItem issue(@PathVariable Long id, @Valid @RequestBody StockMovementRequest request,
                           @RequestHeader(value = "X-User-FullName", required = false) String user) {
        return stockService.issue(id, withUser(request, user));
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Receive stock into a batch")
    public StockItem receive(@PathVariable Long id, @Valid @RequestBody StockMovementRequest request,
                             @RequestHeader(value = "X-User-FullName", required = false) String user) {
        return stockService.receive(id, withUser(request, user));
    }

    @PostMapping("/{id}/write-off")
    @Operation(summary = "Write off an expired batch")
    public StockItem writeOff(@PathVariable Long id, @RequestHeader(value = "X-User-FullName", required = false) String user) {
        return stockService.writeOffExpired(id, user);
    }

    private static StockMovementRequest withUser(StockMovementRequest r, String user) {
        return r.performedBy() != null && !r.performedBy().isBlank() ? r
                : new StockMovementRequest(r.quantity(), r.issuedTo(), r.residentId(), r.reference(), user, r.notes());
    }
}
