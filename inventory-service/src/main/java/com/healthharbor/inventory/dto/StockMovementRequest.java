package com.healthharbor.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Payload for issuing or receiving stock against a stock record. */
public record StockMovementRequest(@NotNull @Min(1) Integer quantity, String issuedTo, Long residentId,
                                   String reference, String performedBy, String notes) {
}
