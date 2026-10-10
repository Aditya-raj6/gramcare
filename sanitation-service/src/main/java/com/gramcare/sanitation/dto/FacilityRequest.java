package com.gramcare.sanitation.dto;

import com.gramcare.sanitation.entity.SanitationFacility;
import jakarta.validation.constraints.NotBlank;

public class FacilityRequest {
    @NotBlank
    private String facilityName;
    private SanitationFacility.FacilityType facilityType;
    @NotBlank
    private String village;
    private String location;

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }
    public SanitationFacility.FacilityType getFacilityType() { return facilityType; }
    public void setFacilityType(SanitationFacility.FacilityType facilityType) { this.facilityType = facilityType; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
