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
@Table(name = "health_centres")
public class HealthCentre extends BaseEntity {

    public enum CentreType {SUB_CENTRE, PHC, CHC, DISPENSARY, WELLNESS_CENTRE}

    public enum CentreStatus {OPERATIONAL, LIMITED, CLOSED}

    @Column(length = 20, unique = true)
    private String code;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CentreType type;

    @NotBlank
    private String village;

    private String block;
    private String district;
    private String address;
    private String phone;
    private String email;
    private String inChargeName;
    private Integer bedCapacity;
    private Integer populationCovered;
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private CentreStatus status = CentreStatus.OPERATIONAL;
}
