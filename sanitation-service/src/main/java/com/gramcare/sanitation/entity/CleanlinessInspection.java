package com.gramcare.sanitation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "cleanliness_inspections")
public class CleanlinessInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    @JsonIgnore
    private SanitationFacility facility;

    private LocalDate inspectionDate;
    private String inspectedBy;

    @Enumerated(EnumType.STRING)
    private SanitationFacility.CleanlinessStatus observedStatus;

    private String remarks;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SanitationFacility getFacility() { return facility; }
    public void setFacility(SanitationFacility facility) { this.facility = facility; }
    public LocalDate getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }
    public String getInspectedBy() { return inspectedBy; }
    public void setInspectedBy(String inspectedBy) { this.inspectedBy = inspectedBy; }
    public SanitationFacility.CleanlinessStatus getObservedStatus() { return observedStatus; }
    public void setObservedStatus(SanitationFacility.CleanlinessStatus observedStatus) { this.observedStatus = observedStatus; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
