package com.gramcare.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class HealthCampRequest {
    @NotBlank
    private String campName;
    @NotBlank
    private String village;
    private LocalDate campDate;
    private String location;

    public String getCampName() { return campName; }
    public void setCampName(String campName) { this.campName = campName; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public LocalDate getCampDate() { return campDate; }
    public void setCampDate(LocalDate campDate) { this.campDate = campDate; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
