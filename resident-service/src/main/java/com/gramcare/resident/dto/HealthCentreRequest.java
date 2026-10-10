package com.gramcare.resident.dto;

import com.gramcare.resident.entity.HealthCentre;
import jakarta.validation.constraints.NotBlank;

public class HealthCentreRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String village;
    private String address;
    private String contactPhone;
    private String contactEmail;
    private HealthCentre.CentreType centreType;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public HealthCentre.CentreType getCentreType() { return centreType; }
    public void setCentreType(HealthCentre.CentreType centreType) { this.centreType = centreType; }
}
