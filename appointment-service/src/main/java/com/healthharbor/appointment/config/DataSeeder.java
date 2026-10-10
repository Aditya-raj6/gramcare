package com.healthharbor.appointment.config;

import com.healthharbor.appointment.entity.Appointment;
import com.healthharbor.appointment.entity.Appointment.AppointmentStatus;
import com.healthharbor.appointment.entity.Appointment.AppointmentType;
import com.healthharbor.appointment.entity.Consultation;
import com.healthharbor.appointment.entity.TreatmentItem;
import com.healthharbor.appointment.repository.AppointmentRepository;
import com.healthharbor.appointment.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Seeds appointments that reference the residents/centres/workers seeded by their own services. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "healthharbor.seed-data", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final AppointmentRepository appointments;
    private final ConsultationRepository consultations;

    @Override
    @Transactional
    public void run(String... args) {
        if (appointments.count() > 0) {
            return;
        }
        LocalDate t = LocalDate.now();
        Appointment a1 = appt(1L, "Ramesh Kumar", "Rampur", 1L, "PHC Rampur", 1L, "Dr. Arjun Patil", t.minusDays(2), "10:00", AppointmentType.CHRONIC_CARE, "Diabetes review", AppointmentStatus.COMPLETED);
        Appointment a2 = appt(5L, "Anil Verma", "Kishanganj", 3L, "CHC Kishanganj", 7L, "Dr. Sameer Khan", t.minusDays(1), "11:30", AppointmentType.GENERAL, "Loose motions since 2 days", AppointmentStatus.COMPLETED);
        appt(2L, "Sita Devi", "Rampur", 2L, "Sub-Centre Sundarpur", 5L, "Kavita Rao", t, "09:30", AppointmentType.ANTENATAL, "ANC 3rd visit", AppointmentStatus.CHECKED_IN);
        appt(14L, "Gopal Mishra", "Sundarpur", 2L, "Sub-Centre Sundarpur", null, null, t, "11:00", AppointmentType.FOLLOW_UP, "BP review", AppointmentStatus.SCHEDULED);
        Appointment a5 = appt(8L, "Pooja Sharma", "Devgarh", 4L, "Sub-Centre Devgarh", 10L, "Dr. Neha Gupta", t, "12:15", AppointmentType.GENERAL, "Persistent weakness", AppointmentStatus.SCHEDULED);
        a5.setPriority(Appointment.Priority.URGENT);
        appointments.save(a5);
        appt(6L, "Lakshmi Bai", "Kishanganj", 3L, "CHC Kishanganj", 7L, "Dr. Sameer Khan", t.minusDays(3), "10:45", AppointmentType.CHRONIC_CARE, "Hypertension check", AppointmentStatus.SCHEDULED);
        appt(10L, "Raju Prasad", "Bhimnagar", 1L, "PHC Rampur", null, null, t.plusDays(1), "10:00", AppointmentType.IMMUNIZATION, "DPT booster", AppointmentStatus.SCHEDULED);
        appt(13L, "Asha Patel", "Lakshmipur", 3L, "CHC Kishanganj", 8L, "Dr. Priya Nair", t.plusDays(2), "14:00", AppointmentType.ANTENATAL, "First trimester scan", AppointmentStatus.SCHEDULED);
        appt(12L, "Vikram Singh", "Lakshmipur", 1L, "PHC Rampur", 1L, "Dr. Arjun Patil", t.plusDays(3), "10:30", AppointmentType.CHRONIC_CARE, "Referred from NCD camp", AppointmentStatus.SCHEDULED);
        appt(3L, "Mohan Lal", "Sundarpur", 2L, "Sub-Centre Sundarpur", 10L, "Dr. Neha Gupta", t.plusDays(5), "11:00", AppointmentType.FOLLOW_UP, "COPD follow-up", AppointmentStatus.SCHEDULED);
        appt(9L, "Kamla Devi", "Rampur", 1L, "PHC Rampur", 1L, "Dr. Arjun Patil", t.minusDays(6), "09:45", AppointmentType.EYE, "Blurred vision", AppointmentStatus.NO_SHOW);
        appt(7L, "Suresh Yadav", "Devgarh", 4L, "Sub-Centre Devgarh", 10L, "Dr. Neha Gupta", t.minusDays(4), "15:00", AppointmentType.GENERAL, "Back pain", AppointmentStatus.CANCELLED);

        consult(a1, "Increased thirst, tingling feet", "Type 2 Diabetes Mellitus", "140/90", 36.9, 78, 98, 72.0,
                "Advise diet control and daily walk", List.of(med("Metformin 500mg", "1 tab", "Twice daily", 30, 60),
                        med("Amlodipine 5mg", "1 tab", "Once daily", 30, 30)));
        consult(a2, "Watery stools x6/day, mild dehydration", "Acute Gastroenteritis", "100/60", 37.9, 104, 97, 64.0,
                "ORS after every stool; boil drinking water", List.of(med("ORS sachet", "1 sachet in 1L water", "After each stool", 3, 10),
                        med("Zinc 20mg", "1 tab", "Once daily", 14, 14)));
        log.info("Seeded {} appointments and {} consultations", appointments.count(), consultations.count());
    }

    private Appointment appt(Long residentId, String resident, String village, Long centreId, String centre, Long workerId,
                             String worker, LocalDate date, String time, AppointmentType type, String reason, AppointmentStatus status) {
        Appointment a = new Appointment();
        a.setResidentId(residentId);
        a.setResidentName(resident);
        a.setVillage(village);
        a.setHealthCentreId(centreId);
        a.setHealthCentreName(centre);
        a.setWorkerId(workerId);
        a.setWorkerName(worker);
        a.setAppointmentDate(date);
        a.setAppointmentTime(LocalTime.parse(time));
        a.setType(type);
        a.setReason(reason);
        a.setStatus(status);
        a.setBookedBy("worker");
        a = appointments.save(a);
        a.setAppointmentNumber(String.format("APT-%05d", a.getId()));
        return appointments.save(a);
    }

    private void consult(Appointment a, String symptoms, String diagnosis, String bp, double temp, int pulse, int spo2,
                         double weight, String advice, List<TreatmentItem> items) {
        Consultation c = new Consultation();
        c.setAppointmentId(a.getId());
        c.setAppointmentNumber(a.getAppointmentNumber());
        c.setResidentId(a.getResidentId());
        c.setResidentName(a.getResidentName());
        c.setHealthCentreId(a.getHealthCentreId());
        c.setHealthCentreName(a.getHealthCentreName());
        c.setDoctorName(a.getWorkerName());
        c.setConsultationDate(a.getAppointmentDate());
        c.setSymptoms(symptoms);
        c.setDiagnosis(diagnosis);
        c.setBloodPressure(bp);
        c.setTemperatureC(temp);
        c.setPulse(pulse);
        c.setSpo2(spo2);
        c.setWeightKg(weight);
        c.setAdvice(advice);
        c.setTreatments(items);
        c = consultations.save(c);
        a.setConsultationId(c.getId());
        appointments.save(a);
    }

    private static TreatmentItem med(String name, String dosage, String frequency, int days, int qty) {
        TreatmentItem t = new TreatmentItem();
        t.setTreatmentType(TreatmentItem.TreatmentType.MEDICATION);
        t.setName(name);
        t.setDosage(dosage);
        t.setFrequency(frequency);
        t.setDurationDays(days);
        t.setQuantity(qty);
        return t;
    }
}
