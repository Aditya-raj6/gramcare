package com.healthharbor.healthcamp.entity;

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
@Table(name = "health_camps")
public class HealthCamp extends BaseEntity {

    public enum CampType {GENERAL_CHECKUP, EYE_CARE, DENTAL, MATERNAL_CHILD_HEALTH, VACCINATION, NCD_SCREENING, TB_SCREENING,
        BLOOD_DONATION, AWARENESS, MENTAL_HEALTH}

    public enum CampStatus {PLANNED, ONGOING, COMPLETED, CANCELLED}

    @Column(length = 20)
    private String campCode;

    @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private CampType campType;

    @NotBlank
    private String village;

    private String venue;

    @NotNull
    private LocalDate startDate;
    private LocalDate endDate;

    private String organizer;
    private Long healthCentreId;
    private String healthCentreName;
    private Integer targetBeneficiaries;
    private Double budget;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private CampStatus status = CampStatus.PLANNED;

    @Column(length = 1000)
    private String outcomeSummary;
}
