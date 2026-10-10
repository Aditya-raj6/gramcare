package com.gramcare.appointment.config;

import com.gramcare.appointment.entity.*;
import com.gramcare.appointment.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Fictional appointments and camps. Resident/worker/centre ids refer to the
 * records created by resident-service's seeder on a fresh database.
 * Appointment times are relative to "now" so "today's appointments" always
 * has data on the day of the demo.
 */
@Component
@Profile("!test")
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final AppointmentRepository appointmentRepository;
    private final ConsultationRepository consultationRepository;
    private final TreatmentRepository treatmentRepository;
    private final HealthCampRepository campRepository;
    private final CampStaffRepository staffRepository;
    private final CampBeneficiaryRepository beneficiaryRepository;
    private final ScreeningRecordRepository screeningRepository;
    private final FollowUpRepository followUpRepository;

    public DemoDataSeeder(AppointmentRepository appointmentRepository, ConsultationRepository consultationRepository,
                           TreatmentRepository treatmentRepository, HealthCampRepository campRepository,
                           CampStaffRepository staffRepository, CampBeneficiaryRepository beneficiaryRepository,
                           ScreeningRecordRepository screeningRepository, FollowUpRepository followUpRepository) {
        this.appointmentRepository = appointmentRepository;
        this.consultationRepository = consultationRepository;
        this.treatmentRepository = treatmentRepository;
        this.campRepository = campRepository;
        this.staffRepository = staffRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.screeningRepository = screeningRepository;
        this.followUpRepository = followUpRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (appointmentRepository.count() == 0) {
            LocalDate today = LocalDate.now();
            appointment(1L, 1L, today.atTime(LocalTime.of(10, 30)), "Blood pressure review", Appointment.AppointmentStatus.SCHEDULED);
            appointment(4L, 2L, today.atTime(LocalTime.of(11, 15)), "Diabetes follow-up", Appointment.AppointmentStatus.SCHEDULED);
            appointment(6L, 3L, today.atTime(LocalTime.of(15, 0)), "Asthma review", Appointment.AppointmentStatus.SCHEDULED);
            appointment(3L, 2L, today.plusDays(2).atTime(LocalTime.of(10, 0)), "Antenatal check-up", Appointment.AppointmentStatus.SCHEDULED);
            appointment(9L, 1L, today.plusDays(5).atTime(LocalTime.of(9, 30)), "Immunisation (child)", Appointment.AppointmentStatus.SCHEDULED);
            Appointment past = appointment(2L, 1L, today.minusDays(3).atTime(LocalTime.of(12, 0)), "Wound follow-up", Appointment.AppointmentStatus.COMPLETED);

            Consultation c = new Consultation();
            c.setAppointment(past);
            c.setSymptoms("Healing wound on left forearm");
            c.setDiagnosis("Superficial laceration, healing well");
            c.setNotes("No signs of infection");
            consultationRepository.save(c);
            Treatment t = new Treatment();
            t.setConsultation(c);
            t.setMedicationPrescribed("Povidone-iodine dressing");
            t.setDosageInstructions("Change dressing daily");
            t.setFollowUpDate(today.plusDays(4));
            treatmentRepository.save(t);
            log.info("Seeded demo appointments");
        }

        if (campRepository.count() == 0) {
            HealthCamp eye = camp("Kondapur Eye Screening Camp", "Kondapur", LocalDate.now().minusDays(6), "Zilla Parishad School", HealthCamp.CampStatus.COMPLETED);
            HealthCamp ncd = camp("Anandpur NCD Screening Camp", "Anandpur", LocalDate.now().plusDays(4), "Gram Panchayat Hall", HealthCamp.CampStatus.PLANNED);
            camp("Rampur Maternal & Child Health Camp", "Rampur", LocalDate.now().plusDays(11), "Anganwadi Centre", HealthCamp.CampStatus.PLANNED);

            staff(eye, 6L, "Dr. M. Iyer", "Doctor");
            staff(eye, 3L, "Kavita Jadhav", "ASHA Worker");
            staff(ncd, 4L, "Meena Shinde", "ANM");

            CampBeneficiary b1 = beneficiary(eye, 1L, "Lakshmi Bai Patil", 58);
            CampBeneficiary b2 = beneficiary(eye, 2L, "Ganesh Shinde", 41);
            beneficiary(eye, 9L, "Anita Deshmukh", 30);
            screening(b1, "Vision", "Reduced near vision, possible cataract", true);
            screening(b2, "Vision", "Normal", false);
            FollowUp f = new FollowUp();
            f.setBeneficiary(b1);
            f.setScheduledDate(LocalDate.now().plusDays(7));
            f.setNotes("Refer to district hospital ophthalmology");
            followUpRepository.save(f);
            log.info("Seeded demo health camps");
        }
    }

    private Appointment appointment(Long residentId, Long centreId, LocalDateTime when, String reason, Appointment.AppointmentStatus status) {
        Appointment a = new Appointment();
        a.setResidentId(residentId);
        a.setHealthCentreId(centreId);
        a.setAppointmentDateTime(when);
        a.setReason(reason);
        a.setStatus(status);
        return appointmentRepository.save(a);
    }

    private HealthCamp camp(String name, String village, LocalDate date, String location, HealthCamp.CampStatus status) {
        HealthCamp c = new HealthCamp();
        c.setCampName(name);
        c.setVillage(village);
        c.setCampDate(date);
        c.setLocation(location);
        c.setStatus(status);
        return campRepository.save(c);
    }

    private void staff(HealthCamp camp, Long workerId, String name, String role) {
        CampStaff s = new CampStaff();
        s.setHealthCamp(camp);
        s.setWorkerId(workerId);
        s.setWorkerName(name);
        s.setRole(role);
        staffRepository.save(s);
    }

    private CampBeneficiary beneficiary(HealthCamp camp, Long residentId, String name, int age) {
        CampBeneficiary b = new CampBeneficiary();
        b.setHealthCamp(camp);
        b.setResidentId(residentId);
        b.setBeneficiaryName(name);
        b.setAge(age);
        return beneficiaryRepository.save(b);
    }

    private void screening(CampBeneficiary b, String type, String result, boolean referral) {
        ScreeningRecord s = new ScreeningRecord();
        s.setBeneficiary(b);
        s.setScreeningType(type);
        s.setResult(result);
        s.setReferralNeeded(referral);
        screeningRepository.save(s);
    }
}
