package com.healthharbor.patient.entity;

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
@Table(name = "medical_records", indexes = @Index(columnList = "residentId"))
public class MedicalRecord extends BaseEntity {

    public enum RecordType {DIAGNOSIS, LAB_RESULT, VACCINATION, PRESCRIPTION, ALLERGY, SURGERY, ANTENATAL, NOTE}

    @NotNull
    private Long residentId;

    private String residentName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RecordType recordType;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 2000)
    private String description;

    private LocalDate recordDate;
    private String recordedBy;
    private String healthCentreName;
}
