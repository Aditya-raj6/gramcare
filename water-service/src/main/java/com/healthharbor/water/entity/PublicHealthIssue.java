package com.healthharbor.water.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Water supply or sanitation-related public-health issue requiring corrective action. */
@Getter
@Setter
@Entity
@Table(name = "public_health_issues", indexes = @Index(columnList = "status"))
public class PublicHealthIssue extends BaseEntity {

    public enum IssueCategory {WATER_CONTAMINATION, SUPPLY_DISRUPTION, PIPELINE_LEAKAGE, STAGNANT_WATER, MOSQUITO_BREEDING,
        WATERBORNE_DISEASE_CLUSTER, OPEN_DEFECATION, SOLID_WASTE, OTHER}

    public enum Severity {LOW, MEDIUM, HIGH, CRITICAL}

    public enum IssueStatus {OPEN, INVESTIGATING, ACTION_TAKEN, RESOLVED, CLOSED}

    public enum IssueSource {MANUAL, QUALITY_TEST, FIELD_REPORT, SURVEILLANCE}

    @Column(length = 20)
    private String issueNumber;

    private Long sourceId;
    private String sourceName;

    @NotBlank
    private String village;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private IssueCategory category;

    @NotBlank
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Severity severity = Severity.MEDIUM;

    private Integer affectedPopulation;
    private Integer suspectedCases;
    private String reportedBy;
    private LocalDate reportedDate;
    private String assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private IssueStatus status = IssueStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private IssueSource source = IssueSource.MANUAL;

    private LocalDate resolvedDate;

    @Column(length = 1000)
    private String resolutionNotes;
}
