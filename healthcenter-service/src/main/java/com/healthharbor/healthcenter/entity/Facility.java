package com.healthharbor.healthcenter.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "centre_facilities", indexes = @Index(columnList = "centreId"))
public class Facility extends BaseEntity {

    public enum FacilityCategory {LABORATORY, PHARMACY, LABOUR_ROOM, X_RAY, AMBULANCE, INPATIENT_BEDS, VACCINATION_COLD_CHAIN,
        OPERATION_THEATRE, TELEMEDICINE, DRINKING_WATER, POWER_BACKUP, TOILETS}

    public enum FacilityStatus {AVAILABLE, UNDER_MAINTENANCE, UNAVAILABLE}

    @NotNull
    private Long centreId;
    private String centreName;

    @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FacilityCategory category;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FacilityStatus status = FacilityStatus.AVAILABLE;

    @Column(length = 500)
    private String notes;
}
