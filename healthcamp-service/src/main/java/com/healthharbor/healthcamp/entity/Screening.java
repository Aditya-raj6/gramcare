package com.healthharbor.healthcamp.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "camp_screenings", indexes = {@Index(columnList = "campId"), @Index(columnList = "beneficiaryId")})
public class Screening extends BaseEntity {

    public enum ScreeningType {BLOOD_PRESSURE, BLOOD_SUGAR, BMI, HAEMOGLOBIN, VISION, DENTAL, TB_SYMPTOMS, ORAL_CANCER,
        BREAST_EXAM, CHILD_GROWTH, MALARIA}

    public enum Outcome {NORMAL, ABNORMAL, CRITICAL}

    private Long campId;
    private String campName;

    @NotNull
    private Long beneficiaryId;
    private String beneficiaryName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ScreeningType screeningType;

    /** Measured value with units, e.g. "150/95 mmHg", "210 mg/dL". */
    private String resultValue;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Outcome outcome;

    private Boolean referred = false;
    private String referredTo;
    private String screenedBy;
    private LocalDate screeningDate;

    @Column(length = 500)
    private String notes;
}
