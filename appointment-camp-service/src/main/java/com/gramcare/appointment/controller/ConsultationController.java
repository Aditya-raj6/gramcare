package com.gramcare.appointment.controller;

import com.gramcare.appointment.dto.ConsultationRequest;
import com.gramcare.appointment.dto.TreatmentRequest;
import com.gramcare.appointment.entity.Treatment;
import com.gramcare.appointment.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final AppointmentService appointmentService;

    public ConsultationController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/{id}/treatments")
    public ResponseEntity<Treatment> addTreatment(@PathVariable Long id, @RequestBody TreatmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.addTreatment(id, req));
    }

    @GetMapping("/{id}/treatments")
    public List<Treatment> treatments(@PathVariable Long id) { return appointmentService.getTreatments(id); }
}
