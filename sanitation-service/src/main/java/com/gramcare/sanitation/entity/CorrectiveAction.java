package com.gramcare.sanitation.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "corrective_actions")
public class CorrectiveAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "water_quality_observation_id")
    private Long waterQualityObservationId;

    @Column(name = "sanitation_complaint_id")
    private Long sanitationComplaintId;

    @Column(length = 2000)
    private String actionDescription;

    private String assignedTo;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private ActionStatus status = ActionStatus.PENDING;

    @Column(length = 2000)
    private String followUpNotes;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ActionStatus { PENDING, IN_PROGRESS, COMPLETED }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWaterQualityObservationId() { return waterQualityObservationId; }
    public void setWaterQualityObservationId(Long waterQualityObservationId) { this.waterQualityObservationId = waterQualityObservationId; }
    public Long getSanitationComplaintId() { return sanitationComplaintId; }
    public void setSanitationComplaintId(Long sanitationComplaintId) { this.sanitationComplaintId = sanitationComplaintId; }
    public String getActionDescription() { return actionDescription; }
    public void setActionDescription(String actionDescription) { this.actionDescription = actionDescription; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public ActionStatus getStatus() { return status; }
    public void setStatus(ActionStatus status) { this.status = status; }
    public String getFollowUpNotes() { return followUpNotes; }
    public void setFollowUpNotes(String followUpNotes) { this.followUpNotes = followUpNotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
