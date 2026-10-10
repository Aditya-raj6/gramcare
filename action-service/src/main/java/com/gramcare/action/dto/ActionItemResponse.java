package com.gramcare.action.dto;

import com.gramcare.action.entity.ActionItem;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ActionItemResponse {
    private Long id;
    private String title;
    private String description;
    private ActionItem.Category category;
    private String village;
    private Long healthCentreId;
    private ActionItem.Priority priority;
    private ActionItem.Status status;
    private String reportedBy;
    private String assignedDepartment;
    private String assignedPerson;
    private LocalDateTime createdDate;
    private LocalDate dueDate;
    private LocalDateTime resolvedDate;
    private String resolutionNotes;
    private boolean overdue;
    private String sourceService;
    private String sourceReferenceType;
    private Long sourceReferenceId;

    public static ActionItemResponse from(ActionItem a) {
        ActionItemResponse dto = new ActionItemResponse();
        dto.id = a.getId();
        dto.title = a.getTitle();
        dto.description = a.getDescription();
        dto.category = a.getCategory();
        dto.village = a.getVillage();
        dto.healthCentreId = a.getHealthCentreId();
        dto.priority = a.getPriority();
        dto.status = a.getStatus();
        dto.reportedBy = a.getReportedBy();
        dto.assignedDepartment = a.getAssignedDepartment();
        dto.assignedPerson = a.getAssignedPerson();
        dto.createdDate = a.getCreatedDate();
        dto.dueDate = a.getDueDate();
        dto.resolvedDate = a.getResolvedDate();
        dto.resolutionNotes = a.getResolutionNotes();
        dto.overdue = a.isOverdue();
        dto.sourceService = a.getSourceService();
        dto.sourceReferenceType = a.getSourceReferenceType();
        dto.sourceReferenceId = a.getSourceReferenceId();
        return dto;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public ActionItem.Category getCategory() { return category; }
    public String getVillage() { return village; }
    public Long getHealthCentreId() { return healthCentreId; }
    public ActionItem.Priority getPriority() { return priority; }
    public ActionItem.Status getStatus() { return status; }
    public String getReportedBy() { return reportedBy; }
    public String getAssignedDepartment() { return assignedDepartment; }
    public String getAssignedPerson() { return assignedPerson; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getResolvedDate() { return resolvedDate; }
    public String getResolutionNotes() { return resolutionNotes; }
    public boolean isOverdue() { return overdue; }
    public String getSourceService() { return sourceService; }
    public String getSourceReferenceType() { return sourceReferenceType; }
    public Long getSourceReferenceId() { return sourceReferenceId; }
}
