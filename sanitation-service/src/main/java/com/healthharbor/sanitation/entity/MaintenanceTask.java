package com.healthharbor.sanitation.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "maintenance_tasks", indexes = {@Index(columnList = "facilityId"), @Index(columnList = "status")})
public class MaintenanceTask extends BaseEntity {

    public enum MaintenanceType {CLEANING, REPAIR, PLUMBING, ELECTRICAL, PEST_CONTROL, DESLUDGING, DRAIN_DESILTING, PAINTING, CONSTRUCTION}

    public enum TaskStatus {PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED}

    public enum TaskSource {MANUAL, COMPLAINT, INSPECTION, FIELD_REPORT, SCHEDULED}

    @Column(length = 20)
    private String taskNumber;

    @NotNull
    private Long facilityId;
    private String facilityName;
    private String village;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MaintenanceType maintenanceType;

    @NotBlank
    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private TaskSource source = TaskSource.MANUAL;

    private String assignedTo;
    private String reportedBy;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private Double estimatedCost;
    private Double actualCost;

    @Column(length = 1000)
    private String completionNotes;
}
