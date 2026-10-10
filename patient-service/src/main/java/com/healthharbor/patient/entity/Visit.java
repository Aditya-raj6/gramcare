package com.healthharbor.patient.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "visits", indexes = @Index(columnList = "residentId"))
public class Visit extends BaseEntity {

    public enum VisitType {OPD, EMERGENCY, FOLLOW_UP, HOME_VISIT, HEALTH_CAMP, ANTENATAL, IMMUNIZATION}

    @NotNull
    private Long residentId;

    private String residentName;

    @NotNull
    private LocalDate visitDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VisitType visitType;

    private Long healthCentreId;
    private String healthCentreName;

    private String reason;
    private String diagnosis;
    private String attendedBy;

    private String bloodPressure;
    private Double temperatureC;
    private Integer pulse;
    private Double weightKg;

    @Column(length = 1000)
    private String notes;

    /** Set when the visit was created automatically from a completed consultation. */
    private Long appointmentId;
}
