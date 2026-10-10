package com.healthharbor.healthcamp.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "camp_staff", indexes = @Index(columnList = "campId"))
public class CampStaff extends BaseEntity {

    @NotNull
    private Long campId;
    private String campName;

    /** Optional link to a health worker in healthcenter-service. */
    private Long workerId;

    @NotBlank
    private String name;

    @NotBlank
    private String role;

    private String phone;

    /** Station or duty at the camp, e.g. "Registration desk", "BP & sugar screening". */
    private String assignment;

    private Boolean volunteer = false;
}
