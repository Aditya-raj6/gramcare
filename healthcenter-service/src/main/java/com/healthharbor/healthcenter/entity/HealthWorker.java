package com.healthharbor.healthcenter.entity;

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
@Table(name = "health_workers", indexes = @Index(columnList = "centreId"))
public class HealthWorker extends BaseEntity {

    public enum WorkerRole {DOCTOR, NURSE, ANM, ASHA, PHARMACIST, LAB_TECHNICIAN, MPW, SANITATION_WORKER, ADMIN_STAFF}

    public enum WorkerStatus {ACTIVE, ON_LEAVE, INACTIVE}

    @NotNull
    private Long centreId;
    private String centreName;

    @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private WorkerRole role;

    private String specialization;
    private String qualification;
    private String phone;
    private String email;
    private String registrationNumber;
    private LocalDate joiningDate;
    private String villagesCovered;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private WorkerStatus status = WorkerStatus.ACTIVE;
}
