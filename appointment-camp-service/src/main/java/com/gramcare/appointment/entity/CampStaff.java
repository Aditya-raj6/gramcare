package com.gramcare.appointment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "camp_staff")
public class CampStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_camp_id", nullable = false)
    @JsonIgnore
    private HealthCamp healthCamp;

    /** References a HealthcareWorker in resident-service; kept as an id, not a cross-database FK. */
    private Long workerId;
    private String workerName;
    private String role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public HealthCamp getHealthCamp() { return healthCamp; }
    public void setHealthCamp(HealthCamp healthCamp) { this.healthCamp = healthCamp; }
    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
