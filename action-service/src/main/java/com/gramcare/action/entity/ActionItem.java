package com.gramcare.action.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "action_items",
       uniqueConstraints = @UniqueConstraint(name = "uk_action_source",
               columnNames = {"source_service", "source_reference_type", "source_reference_id"}))
public class ActionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    private String village;
    private Long healthCentreId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    private String reportedBy;
    private String assignedDepartment;
    private String assignedPerson;

    @Column(updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();
    private LocalDate dueDate;
    private LocalDateTime resolvedDate;

    @Column(length = 2000)
    private String resolutionNotes;

    // Idempotency / provenance: which upstream service and record this action
    // item was created from, so a retried request never creates a duplicate.
    @Column(name = "source_service", nullable = false)
    private String sourceService;
    @Column(name = "source_reference_type", nullable = false)
    private String sourceReferenceType;
    @Column(name = "source_reference_id", nullable = false)
    private Long sourceReferenceId;

    @OneToMany(mappedBy = "actionItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ActionHistory> history = new ArrayList<>();

    public enum Category { MEDICINE_STOCK, SANITATION, WATER_QUALITY, HEALTH_CAMP, GENERAL_HEALTH }
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Status { OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED }

    @Transient
    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now())
                && status != Status.RESOLVED && status != Status.CLOSED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public Long getHealthCentreId() { return healthCentreId; }
    public void setHealthCentreId(Long healthCentreId) { this.healthCentreId = healthCentreId; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public String getAssignedDepartment() { return assignedDepartment; }
    public void setAssignedDepartment(String assignedDepartment) { this.assignedDepartment = assignedDepartment; }
    public String getAssignedPerson() { return assignedPerson; }
    public void setAssignedPerson(String assignedPerson) { this.assignedPerson = assignedPerson; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getResolvedDate() { return resolvedDate; }
    public void setResolvedDate(LocalDateTime resolvedDate) { this.resolvedDate = resolvedDate; }
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    public String getSourceService() { return sourceService; }
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }
    public String getSourceReferenceType() { return sourceReferenceType; }
    public void setSourceReferenceType(String sourceReferenceType) { this.sourceReferenceType = sourceReferenceType; }
    public Long getSourceReferenceId() { return sourceReferenceId; }
    public void setSourceReferenceId(Long sourceReferenceId) { this.sourceReferenceId = sourceReferenceId; }
    public List<ActionHistory> getHistory() { return history; }
    public void setHistory(List<ActionHistory> history) { this.history = history; }
}
