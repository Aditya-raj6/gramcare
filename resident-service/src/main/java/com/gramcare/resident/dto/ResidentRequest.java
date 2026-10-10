package com.gramcare.resident.dto;

import com.gramcare.resident.entity.Resident;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class ResidentRequest {
    @NotBlank(message = "fullName is required")
    private String fullName;
    private LocalDate dateOfBirth;
    private Resident.Gender gender;
    @NotBlank(message = "village is required")
    private String village;
    private String householdAddress;
    private String contactPhone;
    private String bloodGroup;
    private String knownAllergies;
    private String chronicConditions;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public Resident.Gender getGender() { return gender; }
    public void setGender(Resident.Gender gender) { this.gender = gender; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getHouseholdAddress() { return householdAddress; }
    public void setHouseholdAddress(String householdAddress) { this.householdAddress = householdAddress; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getKnownAllergies() { return knownAllergies; }
    public void setKnownAllergies(String knownAllergies) { this.knownAllergies = knownAllergies; }
    public String getChronicConditions() { return chronicConditions; }
    public void setChronicConditions(String chronicConditions) { this.chronicConditions = chronicConditions; }
}
