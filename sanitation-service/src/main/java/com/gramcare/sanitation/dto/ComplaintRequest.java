package com.gramcare.sanitation.dto;

import com.gramcare.sanitation.entity.SanitationComplaint;
import jakarta.validation.constraints.NotBlank;

public class ComplaintRequest {
    private Long facilityId;
    @NotBlank
    private String title;
    private String description;
    private String reportedBy;
    private SanitationComplaint.Priority priority;

    public Long getFacilityId() { return facilityId; }
    public void setFacilityId(Long facilityId) { this.facilityId = facilityId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public SanitationComplaint.Priority getPriority() { return priority; }
    public void setPriority(SanitationComplaint.Priority priority) { this.priority = priority; }
}
