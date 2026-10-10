package com.healthharbor.appointment.service;

import com.healthharbor.appointment.client.PatientClient;
import com.healthharbor.appointment.entity.Appointment;
import com.healthharbor.appointment.entity.Appointment.AppointmentStatus;
import com.healthharbor.appointment.entity.Consultation;
import com.healthharbor.appointment.repository.ConsultationRepository;
import com.healthharbor.common.crud.CrudService;
import com.healthharbor.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Recording a consultation completes its appointment, writes a visit into the patient's history
 * (patient-service) and optionally books the follow-up appointment.
 */
@Slf4j
@Service
public class ConsultationService extends CrudService<Consultation> {

    private final ConsultationRepository consultations;
    private final AppointmentService appointments;
    private final PatientClient patients;

    public ConsultationService(ConsultationRepository repository, AppointmentService appointments, PatientClient patients) {
        super(repository, Consultation.class, "Consultation");
        this.consultations = repository;
        this.appointments = appointments;
        this.patients = patients;
    }

    @Override
    protected void beforeSave(Consultation c, Consultation previous) {
        if (previous != null) {
            return;
        }
        Appointment appointment = appointments.get(c.getAppointmentId());
        if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new BusinessException("Cannot record a consultation for a " + appointment.getStatus() + " appointment");
        }
        if (consultations.findByAppointmentId(appointment.getId()).isPresent()) {
            throw new BusinessException("A consultation already exists for appointment " + appointment.getAppointmentNumber(),
                    HttpStatus.CONFLICT);
        }
        c.setAppointmentNumber(appointment.getAppointmentNumber());
        c.setResidentId(appointment.getResidentId());
        c.setResidentName(appointment.getResidentName());
        c.setHealthCentreId(appointment.getHealthCentreId());
        c.setHealthCentreName(appointment.getHealthCentreName());
        if (c.getDoctorName() == null || c.getDoctorName().isBlank()) {
            c.setDoctorName(appointment.getWorkerName());
        }
        if (c.getConsultationDate() == null) {
            c.setConsultationDate(LocalDate.now());
        }
        if (c.getFollowUpDate() != null && !c.getFollowUpDate().isAfter(c.getConsultationDate())) {
            throw new BusinessException("Follow-up date must be after the consultation date");
        }
    }

    @Override
    protected void afterCreate(Consultation c) {
        Appointment appointment = appointments.get(c.getAppointmentId());
        appointments.markCompleted(appointment, c.getId());
        if (c.getFollowUpDate() != null) {
            Appointment followUp = new Appointment();
            followUp.setResidentId(appointment.getResidentId());
            followUp.setResidentName(appointment.getResidentName());
            followUp.setVillage(appointment.getVillage());
            followUp.setHealthCentreId(appointment.getHealthCentreId());
            followUp.setHealthCentreName(appointment.getHealthCentreName());
            followUp.setWorkerId(appointment.getWorkerId());
            followUp.setWorkerName(appointment.getWorkerName());
            followUp.setAppointmentDate(c.getFollowUpDate());
            followUp.setAppointmentTime(appointment.getAppointmentTime() == null ? LocalTime.of(10, 0) : appointment.getAppointmentTime());
            followUp.setType(Appointment.AppointmentType.FOLLOW_UP);
            followUp.setReason("Follow-up: " + c.getDiagnosis());
            followUp.setBookedBy("system");
            followUp = appointments.saveDirect(followUp);
            followUp.setAppointmentNumber(String.format("APT-%05d", followUp.getId()));
            appointments.saveDirect(followUp);
            c.setFollowUpBooked(true);
            repository.save(c);
        }
        recordVisit(c, appointment);
    }

    private void recordVisit(Consultation c, Appointment appointment) {
        try {
            patients.createVisit(new PatientClient.VisitDto(c.getResidentId(), c.getConsultationDate(),
                    appointment.getType() == Appointment.AppointmentType.FOLLOW_UP ? "FOLLOW_UP"
                            : appointment.getType() == Appointment.AppointmentType.ANTENATAL ? "ANTENATAL"
                            : appointment.getType() == Appointment.AppointmentType.IMMUNIZATION ? "IMMUNIZATION" : "OPD",
                    c.getHealthCentreId(), c.getHealthCentreName(), appointment.getReason(), c.getDiagnosis(),
                    c.getDoctorName(), c.getBloodPressure(), c.getTemperatureC(), c.getPulse(), c.getWeightKg(),
                    c.getAdvice(), appointment.getId()));
        } catch (RuntimeException ex) {
            // The consultation remains the source of truth; visit history sync is best-effort.
            log.warn("Could not sync visit history for consultation {}: {}", c.getId(), ex.getMessage());
        }
    }
}
