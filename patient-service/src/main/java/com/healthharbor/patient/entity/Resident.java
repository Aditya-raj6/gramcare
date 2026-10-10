package com.healthharbor.patient.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;

@Getter
@Setter
@Entity
@Table(name = "residents", indexes = {@Index(columnList = "village"), @Index(columnList = "residentCode", unique = true)})
public class Resident extends BaseEntity {

    public enum Gender {MALE, FEMALE, OTHER}

    public enum ResidentStatus {ACTIVE, MIGRATED, DECEASED}

    @Column(length = 20)
    private String residentCode;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(length = 80)
    private String lastName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Past
    private LocalDate dateOfBirth;

    private String phone;

    /** Stored masked (last four digits only) to limit exposure of national ID. */
    @Column(length = 20)
    private String aadhaarLast4;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String village;

    private String address;
    private String householdNumber;

    @Column(length = 5)
    private String bloodGroup;

    private Double heightCm;
    private Double weightKg;

    @Column(length = 500)
    private String allergies;

    @Column(length = 500)
    private String chronicConditions;

    @Column(length = 500)
    private String currentMedications;

    private Boolean pregnant;
    private Boolean disability;

    private String emergencyContactName;
    private String emergencyContactPhone;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private ResidentStatus status = ResidentStatus.ACTIVE;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getFullName() {
        return lastName == null || lastName.isBlank() ? firstName : firstName + " " + lastName;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getAge() {
        return dateOfBirth == null ? null : Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}
