package com.healthharbor.inventory.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "replenishment_requests", indexes = @Index(columnList = "status"))
public class ReplenishmentRequest extends BaseEntity {

    public enum RequestStatus {PENDING, APPROVED, ORDERED, FULFILLED, REJECTED}

    public enum Priority {LOW, MEDIUM, HIGH, CRITICAL}

    public enum RequestSource {MANUAL, AUTO_LOW_STOCK, FIELD_REPORT}

    @Column(length = 20)
    private String requestNumber;

    @NotNull
    private Long medicineId;
    private String medicineName;

    @NotNull
    private Long healthCentreId;
    private String healthCentreName;

    @NotNull
    @Min(1)
    private Integer requestedQuantity;

    private Integer currentStock;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private RequestStatus status = RequestStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RequestSource source = RequestSource.MANUAL;

    private String requestedBy;
    private String approvedBy;
    private String assignedTo;

    @Column(length = 500)
    private String reason;

    private LocalDate requiredBy;
    private LocalDate fulfilledDate;

    @Column(length = 500)
    private String notes;
}
