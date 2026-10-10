package com.healthharbor.water.entity;

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
@Table(name = "corrective_actions", indexes = @Index(columnList = "issueId"))
public class CorrectiveAction extends BaseEntity {

    public enum ActionType {CHLORINATION, SOURCE_DISINFECTION, PIPELINE_REPAIR, HANDPUMP_REPAIR, FLUSHING, ALTERNATE_SUPPLY,
        SOURCE_CLOSURE, FOGGING, LARVICIDE, AWARENESS_DRIVE, ORS_DISTRIBUTION, MEDICAL_CAMP, OTHER}

    public enum ActionStatus {PLANNED, IN_PROGRESS, COMPLETED, CANCELLED}

    @NotNull
    private Long issueId;
    private String issueNumber;
    private String village;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private ActionType actionType;

    @NotBlank
    @Column(length = 1000)
    private String description;

    private String assignedTo;
    private LocalDate plannedDate;
    private LocalDate completedDate;
    private Double cost;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private ActionStatus status = ActionStatus.PLANNED;

    @Column(length = 1000)
    private String outcome;
}
