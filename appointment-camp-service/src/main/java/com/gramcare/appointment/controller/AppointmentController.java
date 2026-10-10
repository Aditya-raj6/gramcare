package com.gramcare.appointment.controller;

import com.gramcare.appointment.dto.*;
import com.gramcare.appointment.entity.*;
import com.gramcare.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<Appointment> list(@RequestParam(required = false) Long residentId) {
        return residentId != null ? appointmentService.findByResident(residentId) : appointmentService.findAll();
    }

    @GetMapping("/today")
    public List<Appointment> today() { return appointmentService.findToday(); }

    @GetMapping("/upcoming")
    public List<Appointment> upcoming() { return appointmentService.findUpcoming(); }

    @GetMapping("/{id}")
    public Appointment get(@PathVariable Long id) { return appointmentService.findById(id); }

    @PostMapping
    public ResponseEntity<Appointment> create(@Valid @RequestBody AppointmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(req));
    }

    @PutMapping("/{id}")
    public Appointment updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return appointmentService.updateStatus(id, Appointment.AppointmentStatus.valueOf(body.get("status").toUpperCase()));
    }

    @PostMapping("/{id}/consultations")
    public ResponseEntity<Consultation> addConsultation(@PathVariable Long id, @RequestBody ConsultationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.addConsultation(id, req));
    }

    @GetMapping("/{id}/consultations")
    public List<Consultation> consultations(@PathVariable Long id) { return appointmentService.getConsultations(id); }
}
