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
@Table(name = "water_sources", indexes = @Index(columnList = "village"))
public class WaterSource extends BaseEntity {

    public enum SourceType {HANDPUMP, BOREWELL, OPEN_WELL, PIPED_SUPPLY, OVERHEAD_TANK, POND, RIVER, RO_PLANT, TANKER}

    public enum SourceStatus {FUNCTIONAL, PARTIALLY_FUNCTIONAL, NON_FUNCTIONAL, CONTAMINATED}

    public enum QualityStatus {SAFE, MARGINAL, UNSAFE, UNTESTED}

    @Column(length = 20)
    private String sourceCode;

    @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SourceType type;

    @NotBlank
    private String village;

    private String ward;
    private String landmark;
    private Double latitude;
    private Double longitude;
    private Integer householdsServed;
    private Integer populationServed;

    /** Free text supply timing, e.g. "06:00-08:00 & 17:00-19:00". */
    private String supplySchedule;
    private Double supplyHoursPerDay;
    private Double dailyCapacityLitres;
    private String managedBy;

    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private SourceStatus status = SourceStatus.FUNCTIONAL;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private QualityStatus lastQualityStatus = QualityStatus.UNTESTED;

    private LocalDate lastTestedDate;
    private LocalDate lastChlorinatedDate;
}
