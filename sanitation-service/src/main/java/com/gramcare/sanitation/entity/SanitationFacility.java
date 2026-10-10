package com.gramcare.sanitation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "sanitation_facilities")
public class SanitationFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String facilityName;

    @Enumerated(EnumType.STRING)
    private FacilityType facilityType = FacilityType.PUBLIC_TOILET;

    @NotBlank
    private String village;

    private String location;

    @Enumerated(EnumType.STRING)
    private CleanlinessStatus cleanlinessStatus = CleanlinessStatus.SATISFACTORY;

    private boolean operational = true;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum FacilityType { PUBLIC_TOILET, COMMUNITY_TOILET_BLOCK, WATER_SUPPLY_POINT, WASTE_COLLECTION_POINT, DRAINAGE }
    public enum CleanlinessStatus { EXCELLENT, SATISFACTORY, NEEDS_ATTENTION, POOR }

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }
    public FacilityType getFacilityType() { return facilityType; }
    public void setFacilityType(FacilityType facilityType) { this.facilityType = facilityType; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public CleanlinessStatus getCleanlinessStatus() { return cleanlinessStatus; }
    public void setCleanlinessStatus(CleanlinessStatus cleanlinessStatus) { this.cleanlinessStatus = cleanlinessStatus; }
    public boolean isOperational() { return operational; }
    public void setOperational(boolean operational) { this.operational = operational; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
