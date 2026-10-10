package com.gramcare.appointment.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AppointmentRequest {
    @NotNull
    private Long residentId;
    private Long healthCentreId;
    private Long workerId;
    @NotNull
    private LocalDateTime appointmentDateTime;
    private String reason;

    public Long getResidentId() { return residentId; }
    public void setResidentId(Long residentId) { this.residentId = residentId; }
    public Long getHealthCentreId() { return healthCentreId; }
    public void setHealthCentreId(Long healthCentreId) { this.healthCentreId = healthCentreId; }
    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
