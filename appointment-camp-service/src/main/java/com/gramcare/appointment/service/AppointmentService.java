package com.gramcare.appointment.service;

import com.gramcare.appointment.dto.*;
import com.gramcare.appointment.entity.*;
import com.gramcare.appointment.exception.ResourceNotFoundException;
import com.gramcare.appointment.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ConsultationRepository consultationRepository;
    private final TreatmentRepository treatmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               ConsultationRepository consultationRepository,
                               TreatmentRepository treatmentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.consultationRepository = consultationRepository;
        this.treatmentRepository = treatmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAll() { return appointmentRepository.findAll(); }

    @Transactional(readOnly = true)
    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByResident(Long residentId) { return appointmentRepository.findByResidentId(residentId); }

    @Transactional(readOnly = true)
    public List<Appointment> findToday() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return appointmentRepository.findByAppointmentDateTimeBetween(start, end);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findUpcoming() {
        return appointmentRepository.findByAppointmentDateTimeBetween(LocalDateTime.now(), LocalDateTime.now().plusDays(30))
                .stream().filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED).toList();
    }

    public Appointment create(AppointmentRequest req) {
        if (req.getAppointmentDateTime().isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new IllegalArgumentException("Appointment date/time must not be in the past");
        }
        Appointment a = new Appointment();
        a.setResidentId(req.getResidentId());
        a.setHealthCentreId(req.getHealthCentreId());
        a.setWorkerId(req.getWorkerId());
        a.setAppointmentDateTime(req.getAppointmentDateTime());
        a.setReason(req.getReason());
        return appointmentRepository.save(a);
    }

    public Appointment updateStatus(Long id, Appointment.AppointmentStatus status) {
        Appointment a = findById(id);
        a.setStatus(status);
        return appointmentRepository.save(a);
    }

    public Consultation addConsultation(Long appointmentId, ConsultationRequest req) {
        Appointment appointment = findById(appointmentId);
        Consultation c = new Consultation();
        c.setAppointment(appointment);
        c.setSymptoms(req.getSymptoms());
        c.setDiagnosis(req.getDiagnosis());
        c.setNotes(req.getNotes());
        Consultation saved = consultationRepository.save(c);
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Consultation> getConsultations(Long appointmentId) {
        return consultationRepository.findAllByAppointmentId(appointmentId);
    }

    public Treatment addTreatment(Long consultationId, TreatmentRequest req) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id " + consultationId));
        Treatment t = new Treatment();
        t.setConsultation(consultation);
        t.setMedicationPrescribed(req.getMedicationPrescribed());
        t.setDosageInstructions(req.getDosageInstructions());
        t.setFollowUpDate(req.getFollowUpDate());
        t.setNotes(req.getNotes());
        return treatmentRepository.save(t);
    }

    @Transactional(readOnly = true)
    public List<Treatment> getTreatments(Long consultationId) {
        return treatmentRepository.findByConsultationId(consultationId);
    }
}
