package com.gramcare.appointment.dto;

import java.time.LocalDate;

public class TreatmentRequest {
    private String medicationPrescribed;
    private String dosageInstructions;
    private LocalDate followUpDate;
    private String notes;

    public String getMedicationPrescribed() { return medicationPrescribed; }
    public void setMedicationPrescribed(String medicationPrescribed) { this.medicationPrescribed = medicationPrescribed; }
    public String getDosageInstructions() { return dosageInstructions; }
    public void setDosageInstructions(String dosageInstructions) { this.dosageInstructions = dosageInstructions; }
    public LocalDate getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDate followUpDate) { this.followUpDate = followUpDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
