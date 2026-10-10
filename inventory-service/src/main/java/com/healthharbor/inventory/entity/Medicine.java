package com.healthharbor.inventory.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Catalogue entry for a medicine, vaccine or medical supply. */
@Getter
@Setter
@Entity
@Table(name = "medicines")
public class Medicine extends BaseEntity {

    public enum ItemCategory {MEDICINE, VACCINE, SUPPLY, EQUIPMENT, CONSUMABLE, DIAGNOSTIC_KIT}

    public enum DosageForm {TABLET, CAPSULE, SYRUP, INJECTION, OINTMENT, DROPS, SACHET, VIAL, UNIT, KIT}

    @Column(length = 20, unique = true)
    private String code;

    @NotBlank
    @Column(nullable = false)
    private String name;

    private String genericName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ItemCategory category;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private DosageForm form;

    private String strength;

    /** Unit of issue, e.g. "strip of 10", "bottle", "vial". */
    private String unit;

    private String manufacturer;

    /** Default reorder threshold applied to new stock records. */
    private Integer defaultReorderLevel = 50;

    /** Part of the state Essential Medicines List. */
    private Boolean essential = true;

    @Column(length = 500)
    private String description;
}
