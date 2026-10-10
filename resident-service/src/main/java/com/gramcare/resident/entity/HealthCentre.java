package com.gramcare.resident.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "health_centres")
public class HealthCentre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String village;

    private String address;
    private String contactPhone;
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    private CentreType centreType = CentreType.PRIMARY_HEALTH_CENTRE;

    private boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "healthCentre", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<HealthcareWorker> workers = new ArrayList<>();

    @OneToMany(mappedBy = "healthCentre", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CentreService> services = new ArrayList<>();

    @OneToMany(mappedBy = "healthCentre", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<OperatingSchedule> schedules = new ArrayList<>();

    public enum CentreType { PRIMARY_HEALTH_CENTRE, SUB_CENTRE, COMMUNITY_HEALTH_CENTRE, MOBILE_UNIT }

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public CentreType getCentreType() { return centreType; }
    public void setCentreType(CentreType centreType) { this.centreType = centreType; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<HealthcareWorker> getWorkers() { return workers; }
    public void setWorkers(List<HealthcareWorker> workers) { this.workers = workers; }
    public List<CentreService> getServices() { return services; }
    public void setServices(List<CentreService> services) { this.services = services; }
    public List<OperatingSchedule> getSchedules() { return schedules; }
    public void setSchedules(List<OperatingSchedule> schedules) { this.schedules = schedules; }
}
