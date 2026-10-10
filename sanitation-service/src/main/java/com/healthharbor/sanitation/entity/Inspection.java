package com.healthharbor.sanitation.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** A cleanliness inspection; updates the facility's cleanliness status. */
@Getter
@Setter
@Entity
@Table(name = "sanitation_inspections", indexes = @Index(columnList = "facilityId"))
public class Inspection extends BaseEntity {

    @NotNull
    private Long facilityId;
    private String facilityName;
    private String village;

    private LocalDate inspectionDate;
    private String inspectorName;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private SanitationFacility.CleanlinessStatus cleanlinessStatus;

    private Boolean waterAvailable;
    private Boolean lightingWorking;
    private Boolean soapAvailable;
    private Boolean odourFree;

    @Column(length = 1000)
    private String remarks;

    /** Set when the inspection automatically raised a maintenance task. */
    private Long maintenanceTaskId;
}
