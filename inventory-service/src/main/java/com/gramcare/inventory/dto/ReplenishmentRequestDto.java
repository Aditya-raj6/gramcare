package com.gramcare.inventory.dto;

public class ReplenishmentRequestDto {
    private Long medicineId;
    private int requestedQuantity;
    private String requestedBy;
    private String notes;
    private String village;
    private Long healthCentreId;
    private String priority;

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
    public int getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(int requestedQuantity) { this.requestedQuantity = requestedQuantity; }
    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public Long getHealthCentreId() { return healthCentreId; }
    public void setHealthCentreId(Long healthCentreId) { this.healthCentreId = healthCentreId; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
