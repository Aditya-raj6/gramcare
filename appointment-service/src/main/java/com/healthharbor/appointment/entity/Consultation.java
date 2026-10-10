package com.healthharbor.appointment.entity;

import com.healthharbor.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "consultations", indexes = {@Index(columnList = "appointmentId"), @Index(columnList = "residentId")})
public class Consultation extends BaseEntity {

    @NotNull
    private Long appointmentId;
    private String appointmentNumber;

    private Long residentId;
    private String residentName;
    private Long healthCentreId;
    private String healthCentreName;
    private String doctorName;

    private LocalDate consultationDate;

    @Column(length = 1000)
    private String symptoms;

    @NotBlank
    @Column(length = 500)
    private String diagnosis;

    private String bloodPressure;
    private Double temperatureC;
    private Integer pulse;
    private Integer spo2;
    private Double weightKg;

    @Column(length = 2000)
    private String clinicalNotes;

    @Column(length = 1000)
    private String advice;

    private LocalDate followUpDate;

    /** Whether a follow-up appointment was automatically booked for the follow-up date. */
    private Boolean followUpBooked = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "consultation_treatments", joinColumns = @JoinColumn(name = "consultation_id"))
    @OrderColumn(name = "line_no")
    private List<TreatmentItem> treatments = new ArrayList<>();
}
