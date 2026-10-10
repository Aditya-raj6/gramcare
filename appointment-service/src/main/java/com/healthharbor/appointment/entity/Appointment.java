package com.healthharbor.appointment.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "appointments", indexes = {@Index(columnList = "appointmentDate"), @Index(columnList = "residentId")})
public class Appointment extends BaseEntity {

    public enum AppointmentType {GENERAL, ANTENATAL, IMMUNIZATION, FOLLOW_UP, CHRONIC_CARE, DENTAL, EYE, TELECONSULTATION}

    public enum AppointmentStatus {SCHEDULED, CHECKED_IN, COMPLETED, CANCELLED, NO_SHOW}

    public enum Priority {ROUTINE, URGENT, EMERGENCY}

    @Column(length = 20)
    private String appointmentNumber;

    @NotNull
    private Long residentId;
    private String residentName;
    private String village;

    @NotNull
    private Long healthCentreId;
    private String healthCentreName;

    private Long workerId;
    private String workerName;

    @NotNull
    private LocalDate appointmentDate;

    @NotNull
    private LocalTime appointmentTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AppointmentType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private Priority priority = Priority.ROUTINE;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(length = 500)
    private String notes;

    private String bookedBy;

    /** Consultation that completed this appointment, if any. */
    private Long consultationId;
}
