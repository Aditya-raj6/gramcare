package com.healthharbor.appointment.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class TreatmentItem {

    public enum TreatmentType {MEDICATION, PROCEDURE, LAB_TEST, THERAPY, REFERRAL, COUNSELLING}

    @Enumerated(EnumType.STRING)
    private TreatmentType treatmentType;

    /** Medicine, test or procedure name. */
    private String name;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    private Integer quantity;
    private String instructions;
}
