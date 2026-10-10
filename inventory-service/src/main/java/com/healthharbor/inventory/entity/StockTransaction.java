package com.healthharbor.inventory.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Immutable ledger of every stock movement (receipt, issue, adjustment, expiry write-off). */
@Getter
@Setter
@Entity
@Table(name = "stock_transactions", indexes = {@Index(columnList = "stockItemId"), @Index(columnList = "healthCentreId")})
public class StockTransaction extends BaseEntity {

    public enum TransactionType {RECEIPT, ISSUE, ADJUSTMENT, EXPIRED_WRITE_OFF, TRANSFER_OUT}

    private Long stockItemId;
    private Long medicineId;
    private String medicineName;
    private Long healthCentreId;
    private String healthCentreName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TransactionType type;

    /** Signed change applied to the stock quantity. */
    private Integer quantity;
    private Integer balanceAfter;

    /** Resident, health camp or department that received an issue. */
    private String issuedTo;
    private Long residentId;
    private String reference;
    private String performedBy;
    private LocalDateTime transactionDate;

    @Column(length = 500)
    private String notes;
}
