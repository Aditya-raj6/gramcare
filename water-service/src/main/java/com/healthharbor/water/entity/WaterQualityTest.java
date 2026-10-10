package com.healthharbor.water.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Field or laboratory water-quality observation, evaluated against BIS 10500:2012 limits. */
@Getter
@Setter
@Entity
@Table(name = "water_quality_tests", indexes = @Index(columnList = "sourceId"))
public class WaterQualityTest extends BaseEntity {

    public enum TestMethod {FIELD_TEST_KIT, H2S_VIAL, LABORATORY}

    @NotNull
    private Long sourceId;
    private String sourceName;
    private String village;

    private LocalDate testDate;
    private String testedBy;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TestMethod method = TestMethod.FIELD_TEST_KIT;

    private Double ph;
    /** NTU */
    private Double turbidity;
    /** mg/L */
    private Double tds;
    /** mg/L */
    private Double residualChlorine;
    /** mg/L */
    private Double fluoride;
    /** mg/L */
    private Double nitrate;
    /** mg/L */
    private Double arsenic;
    /** mg/L */
    private Double iron;
    private Boolean coliformPresent;
    private Boolean ecoliPresent;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private WaterSource.QualityStatus overallStatus;

    @Column(length = 1000)
    private String findings;

    @Column(length = 500)
    private String remarks;

    private Long issueId;
}
