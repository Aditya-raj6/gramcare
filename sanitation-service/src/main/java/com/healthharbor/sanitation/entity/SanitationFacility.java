package com.healthharbor.sanitation.entity;

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
@Table(name = "sanitation_facilities", indexes = @Index(columnList = "village"))
public class SanitationFacility extends BaseEntity {

    public enum FacilityType {COMMUNITY_TOILET, PUBLIC_TOILET, SCHOOL_TOILET, DRAINAGE, WASTE_COLLECTION_POINT, SOAK_PIT,
        HANDWASH_STATION, FAECAL_SLUDGE_PLANT, COMPOST_PIT}

    public enum CleanlinessStatus {CLEAN, SATISFACTORY, DIRTY, UNUSABLE}

    public enum OperationalStatus {OPERATIONAL, UNDER_MAINTENANCE, NON_FUNCTIONAL}

    @Column(length = 20)
    private String facilityCode;

    @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private FacilityType type;

    @NotBlank
    private String village;

    private String ward;
    private String landmark;
    private Double latitude;
    private Double longitude;

    /** Seats / capacity in persons per day. */
    private Integer capacity;

    private Boolean waterAvailable = true;
    private Boolean electricityAvailable = true;
    private Boolean accessibleForDisabled = false;
    private Boolean genderSeparated = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private CleanlinessStatus cleanlinessStatus = CleanlinessStatus.SATISFACTORY;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OperationalStatus status = OperationalStatus.OPERATIONAL;

    private LocalDate lastInspectionDate;
    private LocalDate lastCleanedDate;
    private String caretakerName;
    private String caretakerPhone;
    private String managedBy;
}
