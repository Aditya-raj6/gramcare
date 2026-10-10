package com.healthharbor.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;

@FeignClient(name = "patient-service")
public interface PatientClient {

    @GetMapping("/api/residents/{id}")
    ResidentDto getResident(@PathVariable("id") Long id);

    @PostMapping("/api/visits")
    void createVisit(@RequestBody VisitDto visit);

    record ResidentDto(Long id, String fullName, String village, String phone) {
    }

    record VisitDto(Long residentId, LocalDate visitDate, String visitType, Long healthCentreId, String healthCentreName,
                    String reason, String diagnosis, String attendedBy, String bloodPressure, Double temperatureC,
                    Integer pulse, Double weightKg, String notes, Long appointmentId) {
    }
}
