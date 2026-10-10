package com.healthharbor.sanitation.entity;

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
@Table(name = "sanitation_complaints", indexes = @Index(columnList = "status"))
public class Complaint extends BaseEntity {

    public enum ComplaintCategory {UNCLEAN_TOILET, BLOCKED_DRAIN, NO_WATER, BROKEN_FIXTURE, SEWAGE_OVERFLOW, GARBAGE_NOT_COLLECTED,
        OPEN_DEFECATION, BAD_ODOUR, NO_LIGHTING, OTHER}

    public enum ComplaintStatus {OPEN, IN_PROGRESS, RESOLVED, CLOSED, REJECTED}

    @Column(length = 20)
    private String complaintNumber;

    private Long facilityId;
    private String facilityName;

    @NotBlank
    private String village;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private ComplaintCategory category;

    @NotBlank
    @Column(length = 1000)
    private String description;

    private String complainantName;
    private String complainantPhone;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private ComplaintStatus status = ComplaintStatus.OPEN;

    private String assignedTo;
    private LocalDate reportedDate;
    private LocalDate resolvedDate;

    @Column(length = 1000)
    private String resolutionNotes;

    private Long maintenanceTaskId;

    /** "CITIZEN", "HEALTH_WORKER", "FIELD_REPORT" ... */
    private String channel;
}
