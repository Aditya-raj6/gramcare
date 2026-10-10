package com.gramcare.resident.dto;

import com.gramcare.resident.entity.Resident;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ResidentResponse {
    private Long id;
    private String fullName;
    private LocalDate dateOfBirth;
    private Resident.Gender gender;
    private String village;
    private String householdAddress;
    private String contactPhone;
    private String bloodGroup;
    private String knownAllergies;
    private String chronicConditions;
    private LocalDateTime registeredAt;
    private int visitCount;

    public static ResidentResponse from(Resident r) {
        ResidentResponse dto = new ResidentResponse();
        dto.id = r.getId();
        dto.fullName = r.getFullName();
        dto.dateOfBirth = r.getDateOfBirth();
        dto.gender = r.getGender();
        dto.village = r.getVillage();
        dto.householdAddress = r.getHouseholdAddress();
        dto.contactPhone = r.getContactPhone();
        dto.bloodGroup = r.getBloodGroup();
        dto.knownAllergies = r.getKnownAllergies();
        dto.chronicConditions = r.getChronicConditions();
        dto.registeredAt = r.getRegisteredAt();
        dto.visitCount = r.getMedicalRecords() != null ? r.getMedicalRecords().size() : 0;
        return dto;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public Resident.Gender getGender() { return gender; }
    public String getVillage() { return village; }
    public String getHouseholdAddress() { return householdAddress; }
    public String getContactPhone() { return contactPhone; }
    public String getBloodGroup() { return bloodGroup; }
    public String getKnownAllergies() { return knownAllergies; }
    public String getChronicConditions() { return chronicConditions; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public int getVisitCount() { return visitCount; }
}
