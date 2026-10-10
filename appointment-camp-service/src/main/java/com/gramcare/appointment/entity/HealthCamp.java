package com.gramcare.appointment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "health_camps")
public class HealthCamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String campName;

    @NotBlank
    private String village;

    private LocalDate campDate;
    private String location;

    @Enumerated(EnumType.STRING)
    private CampStatus status = CampStatus.PLANNED;

    @OneToMany(mappedBy = "healthCamp", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CampStaff> staff = new ArrayList<>();

    @OneToMany(mappedBy = "healthCamp", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CampBeneficiary> beneficiaries = new ArrayList<>();

    public enum CampStatus { PLANNED, ONGOING, COMPLETED, CANCELLED }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCampName() { return campName; }
    public void setCampName(String campName) { this.campName = campName; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public LocalDate getCampDate() { return campDate; }
    public void setCampDate(LocalDate campDate) { this.campDate = campDate; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public CampStatus getStatus() { return status; }
    public void setStatus(CampStatus status) { this.status = status; }
    public List<CampStaff> getStaff() { return staff; }
    public void setStaff(List<CampStaff> staff) { this.staff = staff; }
    public List<CampBeneficiary> getBeneficiaries() { return beneficiaries; }
    public void setBeneficiaries(List<CampBeneficiary> beneficiaries) { this.beneficiaries = beneficiaries; }
}
