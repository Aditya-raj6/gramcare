package com.gramcare.sanitation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "water_quality_observations")
public class WaterQualityObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String village;
    private String waterSourceName;
    private LocalDate observationDate;

    private BigDecimal ph;
    private BigDecimal turbidityNtu;
    private BigDecimal residualChlorineMgL;

    // Reference limits used to flag an observation for review, recorded with
    // the observation itself so the dashboard shows what was actually measured
    // against what the facility considers acceptable. This is not a certified
    // safety declaration.
    private BigDecimal phMin = BigDecimal.valueOf(6.5);
    private BigDecimal phMax = BigDecimal.valueOf(8.5);
    private BigDecimal turbidityMaxNtu = BigDecimal.valueOf(5);
    private BigDecimal residualChlorineMinMgL = BigDecimal.valueOf(0.2);

    private String recordedBy;
    @Column(length = 2000)
    private String notes;

    @Enumerated(EnumType.STRING)
    private ReviewStatus reviewStatus = ReviewStatus.NOT_FLAGGED;

    private Long actionItemId;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ReviewStatus { NOT_FLAGGED, FLAGGED_FOR_REVIEW, ACTION_TAKEN, CLEARED }

    @Transient
    public boolean isOutOfRange() {
        boolean phOut = ph != null && (ph.compareTo(phMin) < 0 || ph.compareTo(phMax) > 0);
        boolean turbidityOut = turbidityNtu != null && turbidityMaxNtu != null && turbidityNtu.compareTo(turbidityMaxNtu) > 0;
        boolean chlorineOut = residualChlorineMgL != null && residualChlorineMinMgL != null
                && residualChlorineMgL.compareTo(residualChlorineMinMgL) < 0;
        return phOut || turbidityOut || chlorineOut;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getWaterSourceName() { return waterSourceName; }
    public void setWaterSourceName(String waterSourceName) { this.waterSourceName = waterSourceName; }
    public LocalDate getObservationDate() { return observationDate; }
    public void setObservationDate(LocalDate observationDate) { this.observationDate = observationDate; }
    public BigDecimal getPh() { return ph; }
    public void setPh(BigDecimal ph) { this.ph = ph; }
    public BigDecimal getTurbidityNtu() { return turbidityNtu; }
    public void setTurbidityNtu(BigDecimal turbidityNtu) { this.turbidityNtu = turbidityNtu; }
    public BigDecimal getResidualChlorineMgL() { return residualChlorineMgL; }
    public void setResidualChlorineMgL(BigDecimal residualChlorineMgL) { this.residualChlorineMgL = residualChlorineMgL; }
    public BigDecimal getPhMin() { return phMin; }
    public void setPhMin(BigDecimal phMin) { this.phMin = phMin; }
    public BigDecimal getPhMax() { return phMax; }
    public void setPhMax(BigDecimal phMax) { this.phMax = phMax; }
    public BigDecimal getTurbidityMaxNtu() { return turbidityMaxNtu; }
    public void setTurbidityMaxNtu(BigDecimal turbidityMaxNtu) { this.turbidityMaxNtu = turbidityMaxNtu; }
    public BigDecimal getResidualChlorineMinMgL() { return residualChlorineMinMgL; }
    public void setResidualChlorineMinMgL(BigDecimal residualChlorineMinMgL) { this.residualChlorineMinMgL = residualChlorineMinMgL; }
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public ReviewStatus getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(ReviewStatus reviewStatus) { this.reviewStatus = reviewStatus; }
    public Long getActionItemId() { return actionItemId; }
    public void setActionItemId(Long actionItemId) { this.actionItemId = actionItemId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
