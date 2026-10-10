package com.gramcare.action.dto;

import com.gramcare.action.entity.ActionItem;

public class StatusUpdateRequest {
    private ActionItem.Status status;
    private String notes;
    private String changedBy;

    public ActionItem.Status getStatus() { return status; }
    public void setStatus(ActionItem.Status status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
}
