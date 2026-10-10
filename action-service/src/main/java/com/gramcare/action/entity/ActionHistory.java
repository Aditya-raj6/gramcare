package com.gramcare.action.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "action_history")
public class ActionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_item_id", nullable = false)
    @JsonIgnore
    private ActionItem actionItem;

    @Enumerated(EnumType.STRING)
    private ActionItem.Status previousStatus;

    @Enumerated(EnumType.STRING)
    private ActionItem.Status newStatus;

    private String changedBy;
    private String notes;

    @Column(updatable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ActionItem getActionItem() { return actionItem; }
    public void setActionItem(ActionItem actionItem) { this.actionItem = actionItem; }
    public ActionItem.Status getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(ActionItem.Status previousStatus) { this.previousStatus = previousStatus; }
    public ActionItem.Status getNewStatus() { return newStatus; }
    public void setNewStatus(ActionItem.Status newStatus) { this.newStatus = newStatus; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
