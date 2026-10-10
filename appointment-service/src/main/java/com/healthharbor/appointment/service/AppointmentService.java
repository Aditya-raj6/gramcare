package com.healthharbor.appointment.service;

import com.healthharbor.appointment.client.HealthCentreClient;
import com.healthharbor.appointment.client.PatientClient;
import com.healthharbor.appointment.entity.Appointment;
import com.healthharbor.appointment.entity.Appointment.AppointmentStatus;
import com.healthharbor.appointment.repository.AppointmentRepository;
import com.healthharbor.common.crud.CrudService;
import com.healthharbor.common.dto.StatusUpdateRequest;
import com.healthharbor.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.healthharbor.appointment.client.RemoteLookup.fetch;

@Service
public class AppointmentService extends CrudService<Appointment> {

    private static final Set<AppointmentStatus> ACTIVE = EnumSet.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CHECKED_IN);

    private static final Map<AppointmentStatus, Set<AppointmentStatus>> TRANSITIONS = Map.of(
            AppointmentStatus.SCHEDULED, EnumSet.of(AppointmentStatus.CHECKED_IN, AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW),
            AppointmentStatus.CHECKED_IN, EnumSet.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED, AppointmentStatus.SCHEDULED),
            AppointmentStatus.NO_SHOW, EnumSet.of(AppointmentStatus.SCHEDULED),
            AppointmentStatus.CANCELLED, EnumSet.of(AppointmentStatus.SCHEDULED),
            AppointmentStatus.COMPLETED, EnumSet.noneOf(AppointmentStatus.class));

    private final AppointmentRepository appointments;
    private final PatientClient patients;
    private final HealthCentreClient centres;

    public AppointmentService(AppointmentRepository repository, PatientClient patients, HealthCentreClient centres) {
        super(repository, Appointment.class, "Appointment");
        this.appointments = repository;
        this.patients = patients;
        this.centres = centres;
    }

    @Override
    protected void beforeSave(Appointment a, Appointment previous) {
        boolean isNew = previous == null;
        if (isNew && a.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Appointment date cannot be in the past");
        }
        if (isNew || !Objects.equals(a.getResidentId(), previous.getResidentId())) {
            PatientClient.ResidentDto resident = fetch("Resident", a.getResidentId(), () -> patients.getResident(a.getResidentId()));
            a.setResidentName(resident.fullName());
            a.setVillage(resident.village());
        }
        if (isNew || !Objects.equals(a.getHealthCentreId(), previous.getHealthCentreId())) {
            HealthCentreClient.CentreDto centre = fetch("Health centre", a.getHealthCentreId(), () -> centres.getCentre(a.getHealthCentreId()));
            if ("CLOSED".equals(centre.status())) {
                throw new BusinessException(centre.name() + " is currently closed for appointments");
            }
            a.setHealthCentreName(centre.name());
        }
        if (a.getWorkerId() != null && (isNew || !Objects.equals(a.getWorkerId(), previous.getWorkerId()))) {
            HealthCentreClient.WorkerDto worker = fetch("Health worker", a.getWorkerId(), () -> centres.getWorker(a.getWorkerId()));
            a.setWorkerName(worker.name());
        }
        if (a.getWorkerId() != null && ACTIVE.contains(a.getStatus())
                && appointments.existsByWorkerIdAndAppointmentDateAndAppointmentTimeAndStatusInAndIdNot(a.getWorkerId(),
                a.getAppointmentDate(), a.getAppointmentTime(), ACTIVE, a.getId() == null ? -1L : a.getId())) {
            throw new BusinessException(a.getWorkerName() + " already has an appointment at that time", HttpStatus.CONFLICT);
        }
    }

    @Override
    protected void afterCreate(Appointment a) {
        a.setAppointmentNumber(reference("APT", a.getId()));
        repository.save(a);
    }

    @Override
    protected void validateTransition(Appointment entity, Object oldStatus, Object newStatus) {
        if (newStatus == AppointmentStatus.COMPLETED && entity.getConsultationId() == null) {
            throw new BusinessException("Record a consultation to complete this appointment");
        }
        if (oldStatus != newStatus && !TRANSITIONS.get((AppointmentStatus) oldStatus).contains((AppointmentStatus) newStatus)) {
            throw new BusinessException("Cannot move appointment from " + oldStatus + " to " + newStatus);
        }
    }

    @Override
    protected void onStatusChange(Appointment entity, Object oldStatus, Object newStatus, StatusUpdateRequest request) {
        if (request.notes() != null && !request.notes().isBlank()) {
            entity.setNotes(request.notes());
        }
    }

    /** Internal transition used once a consultation is recorded. */
    void markCompleted(Appointment appointment, Long consultationId) {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setConsultationId(consultationId);
        repository.save(appointment);
    }

    Appointment saveDirect(Appointment appointment) {
        return repository.save(appointment);
    }
}
