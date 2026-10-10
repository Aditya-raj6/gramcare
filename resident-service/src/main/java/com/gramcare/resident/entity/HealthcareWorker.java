package com.gramcare.resident.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "healthcare_workers")
public class HealthcareWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String fullName;

    @Enumerated(EnumType.STRING)
    private WorkerRole role = WorkerRole.ANM;

    private String qualification;
    private String contactPhone;
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_centre_id")
    @JsonIgnore
    private HealthCentre healthCentre;

    public enum WorkerRole { DOCTOR, NURSE, ANM, ASHA_WORKER, LAB_TECHNICIAN, PHARMACIST, ADMIN_STAFF }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public WorkerRole getRole() { return role; }
    public void setRole(WorkerRole role) { this.role = role; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public HealthCentre getHealthCentre() { return healthCentre; }
    public void setHealthCentre(HealthCentre healthCentre) { this.healthCentre = healthCentre; }
}
