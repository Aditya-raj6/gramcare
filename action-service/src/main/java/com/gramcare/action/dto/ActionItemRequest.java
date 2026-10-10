package com.gramcare.action.dto;

import com.gramcare.action.entity.ActionItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ActionItemRequest {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private ActionItem.Category category;
    private String village;
    private Long healthCentreId;
    private ActionItem.Priority priority;
    private String reportedBy;
    @NotBlank
    private String sourceService;
    @NotBlank
    private String sourceReferenceType;
    @NotNull
    private Long sourceReferenceId;
    private LocalDate dueDate;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ActionItem.Category getCategory() { return category; }
    public void setCategory(ActionItem.Category category) { this.category = category; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public Long getHealthCentreId() { return healthCentreId; }
    public void setHealthCentreId(Long healthCentreId) { this.healthCentreId = healthCentreId; }
    public ActionItem.Priority getPriority() { return priority; }
    public void setPriority(ActionItem.Priority priority) { this.priority = priority; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public String getSourceService() { return sourceService; }
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }
    public String getSourceReferenceType() { return sourceReferenceType; }
    public void setSourceReferenceType(String sourceReferenceType) { this.sourceReferenceType = sourceReferenceType; }
    public Long getSourceReferenceId() { return sourceReferenceId; }
    public void setSourceReferenceId(Long sourceReferenceId) { this.sourceReferenceId = sourceReferenceId; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
