package com.gramcare.resident.dto;

import java.time.LocalDate;

public class MedicalRecordRequest {
    private LocalDate visitDate;
    private String reasonForVisit;
    private String diagnosisSummary;
    private String notes;
    private Long healthCentreId;

    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }
    public String getReasonForVisit() { return reasonForVisit; }
    public void setReasonForVisit(String reasonForVisit) { this.reasonForVisit = reasonForVisit; }
    public String getDiagnosisSummary() { return diagnosisSummary; }
    public void setDiagnosisSummary(String diagnosisSummary) { this.diagnosisSummary = diagnosisSummary; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getHealthCentreId() { return healthCentreId; }
    public void setHealthCentreId(Long healthCentreId) { this.healthCentreId = healthCentreId; }
}
