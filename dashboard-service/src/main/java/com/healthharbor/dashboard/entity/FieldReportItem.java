package com.healthharbor.dashboard.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "field_report_items")
public class FieldReportItem {

    public enum ItemType {
        /** → sanitation-service maintenance task */
        SANITATION_MAINTENANCE,
        /** → sanitation-service complaint */
        SANITATION_COMPLAINT,
        /** → inventory-service replenishment request */
        MEDICINE_SHORTAGE,
        /** → water-service public-health issue */
        WATER_ISSUE
    }

    public enum RoutingStatus {ROUTED, FAILED}

    public enum Priority {LOW, MEDIUM, HIGH, CRITICAL}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private FieldReport report;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ItemType itemType;

    @NotBlank
    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Priority priority = Priority.MEDIUM;

    /* type-specific references */
    private Long facilityId;
    private String facilityName;
    private String category;
    private Long healthCentreId;
    private String healthCentreName;
    private Long medicineId;
    private String medicineName;
    private Integer quantity;
    private Long waterSourceId;
    private String waterSourceName;

    /* routing outcome */
    private String targetService;
    private Long targetId;
    private String targetReference;
    private String targetStatus;
    private String targetPath;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private RoutingStatus routingStatus;

    @Column(length = 500)
    private String routingMessage;

    private LocalDateTime lastSyncedAt;
}
