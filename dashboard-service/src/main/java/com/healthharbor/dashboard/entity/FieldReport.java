package com.healthharbor.dashboard.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A report submitted by a field worker that may contain several issues (e.g. a sanitation facility
 * needing maintenance AND a health centre running low on medicine). Each item is routed to the
 * microservice that owns it.
 */
@Getter
@Setter
@Entity
@Table(name = "field_reports")
public class FieldReport extends BaseEntity {

    @Column(length = 20)
    private String reportNumber;

    @NotBlank
    private String village;

    private String reportedBy;
    private String reporterRole;
    private LocalDate reportDate;

    @Column(length = 1000)
    private String summary;

    @Valid
    @NotEmpty
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id")
    private List<FieldReportItem> items = new ArrayList<>();
}
