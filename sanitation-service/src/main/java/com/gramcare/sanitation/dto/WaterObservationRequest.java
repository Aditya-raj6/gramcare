package com.gramcare.sanitation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class WaterObservationRequest {
    private String village;
    private String waterSourceName;
    private LocalDate observationDate;
    private BigDecimal ph;
    private BigDecimal turbidityNtu;
    private BigDecimal residualChlorineMgL;
    private String recordedBy;
    private String notes;

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
    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
