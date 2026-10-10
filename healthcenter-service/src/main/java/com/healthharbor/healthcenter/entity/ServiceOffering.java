package com.healthharbor.healthcenter.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** A healthcare service offered at a centre (OPD, immunisation, ANC, TB DOTS...). */
@Getter
@Setter
@Entity
@Table(name = "centre_services", indexes = @Index(columnList = "centreId"))
public class ServiceOffering extends BaseEntity {

    public enum ServiceCategory {GENERAL_OPD, MATERNAL_HEALTH, CHILD_HEALTH, IMMUNIZATION, FAMILY_PLANNING, COMMUNICABLE_DISEASE,
        NCD, DIAGNOSTICS, EMERGENCY, DENTAL, EYE_CARE, MENTAL_HEALTH}

    @NotNull
    private Long centreId;
    private String centreName;

    @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ServiceCategory category;

    @Column(length = 500)
    private String description;

    /** Fee in INR; 0 for free government services. */
    private Double fee = 0.0;

    /** e.g. "Mon, Wed, Fri" */
    private String availableDays;

    private Boolean available = true;
}
