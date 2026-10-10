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
@Table(name = "camp_beneficiaries", indexes = @Index(columnList = "campId"))
public class Beneficiary extends BaseEntity {

    public enum Gender {MALE, FEMALE, OTHER}

    @NotNull
    private Long campId;
    private String campName;

    /** Optional link to a registered resident in patient-service. */
    private Long residentId;

    @NotBlank
    private String name;

    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    private String village;
    private String phone;
    private LocalDate registeredOn;

    @Column(length = 500)
    private String servicesReceived;
}
