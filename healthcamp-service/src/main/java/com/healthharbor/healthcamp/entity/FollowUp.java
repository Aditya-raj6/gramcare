package com.healthharbor.healthcamp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "camp_follow_ups", indexes = {@Index(columnList = "campId"), @Index(columnList = "status")})
public class FollowUp extends BaseEntity {

    public enum FollowUpStatus {PENDING, IN_PROGRESS, COMPLETED, MISSED}

    public enum Priority {LOW, MEDIUM, HIGH, CRITICAL}

    private Long campId;
    private String campName;

    @NotNull
    private Long beneficiaryId;
    private String beneficiaryName;
    private String village;

    private Long screeningId;

    @NotBlank
    @Column(length = 500)
    private String action;

    @NotNull
    private LocalDate dueDate;

    private String assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private FollowUpStatus status = FollowUpStatus.PENDING;

    private LocalDate completedDate;

    @Column(length = 500)
    private String outcomeNotes;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now())
                && (status == FollowUpStatus.PENDING || status == FollowUpStatus.IN_PROGRESS);
    }
}
