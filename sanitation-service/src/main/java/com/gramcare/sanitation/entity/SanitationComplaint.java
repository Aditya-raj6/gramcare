package com.gramcare.sanitation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "sanitation_complaints")
public class SanitationComplaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "facility_id")
    @JsonIgnore
    private SanitationFacility facility;

    @NotBlank
    private String title;

    @Column(length = 2000)
    private String description;

    private String reportedBy;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status = ComplaintStatus.OPEN;

    private String assignedWorker;
    @Column(length = 2000)
    private String closureNotes;

    /** Identifier of the linked action item in the Action Tracking Service, if created. */
    private Long actionItemId;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime resolvedAt;

    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    public enum ComplaintStatus { OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED }

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public String getFacilityName() { return facility != null ? facility.getFacilityName() : null; }
    public String getVillage() { return facility != null ? facility.getVillage() : null; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SanitationFacility getFacility() { return facility; }
    public void setFacility(SanitationFacility facility) { this.facility = facility; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }
    public String getAssignedWorker() { return assignedWorker; }
    public void setAssignedWorker(String assignedWorker) { this.assignedWorker = assignedWorker; }
    public String getClosureNotes() { return closureNotes; }
    public void setClosureNotes(String closureNotes) { this.closureNotes = closureNotes; }
    public Long getActionItemId() { return actionItemId; }
    public void setActionItemId(Long actionItemId) { this.actionItemId = actionItemId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
