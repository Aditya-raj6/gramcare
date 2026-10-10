package com.healthharbor.inventory.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Quantity of one catalogue item held at one health centre (per batch). */
@Getter
@Setter
@Entity
@Table(name = "stock_items", indexes = {@Index(columnList = "healthCentreId"), @Index(columnList = "medicineId")})
public class StockItem extends BaseEntity {

    public enum StockStatus {IN_STOCK, LOW_STOCK, OUT_OF_STOCK, EXPIRED}

    @NotNull
    private Long medicineId;
    private String medicineName;
    private String category;

    @NotNull
    private Long healthCentreId;
    @NotNull
    private String healthCentreName;

    private String batchNumber;

    @NotNull
    @Min(0)
    private Integer quantity;

    @Min(0)
    private Integer reorderLevel;

    private LocalDate expiryDate;
    private Double unitPrice;
    private LocalDate lastRestockedDate;
    private String storageLocation;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public StockStatus getStockStatus() {
        if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
            return StockStatus.EXPIRED;
        }
        if (quantity == null || quantity <= 0) {
            return StockStatus.OUT_OF_STOCK;
        }
        return reorderLevel != null && quantity <= reorderLevel ? StockStatus.LOW_STOCK : StockStatus.IN_STOCK;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isExpiringSoon() {
        return expiryDate != null && !expiryDate.isBefore(LocalDate.now()) && expiryDate.isBefore(LocalDate.now().plusDays(60));
    }
}
