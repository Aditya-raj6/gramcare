package com.gramcare.resident.config;

import com.gramcare.resident.entity.*;
import com.gramcare.resident.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Fictional demo data (no real patients). Inserted only when the residents
 * and health-centre tables are empty, so restarts never duplicate it.
 */
@Component
@Profile("!test")
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final ResidentRepository residentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final HealthCentreRepository centreRepository;
    private final HealthcareWorkerRepository workerRepository;
    private final CentreServiceRepository serviceRepository;
    private final OperatingScheduleRepository scheduleRepository;

    public DemoDataSeeder(ResidentRepository residentRepository, MedicalRecordRepository medicalRecordRepository,
                           HealthCentreRepository centreRepository, HealthcareWorkerRepository workerRepository,
                           CentreServiceRepository serviceRepository, OperatingScheduleRepository scheduleRepository) {
        this.residentRepository = residentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.centreRepository = centreRepository;
        this.workerRepository = workerRepository;
        this.serviceRepository = serviceRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (centreRepository.count() == 0) {
            HealthCentre kondapur = centre("Kondapur Primary Health Centre", "Kondapur", HealthCentre.CentreType.PRIMARY_HEALTH_CENTRE, "08422-240101");
            HealthCentre anandpur = centre("Anandpur Sub Centre", "Anandpur", HealthCentre.CentreType.SUB_CENTRE, "08422-240202");
            HealthCentre shivnagar = centre("Shivnagar Community Health Centre", "Shivnagar", HealthCentre.CentreType.COMMUNITY_HEALTH_CENTRE, "08422-240303");
            HealthCentre rampur = centre("Rampur Mobile Health Unit", "Rampur", HealthCentre.CentreType.MOBILE_UNIT, "08422-240404");

            worker(kondapur, "Dr. N. Rao", HealthcareWorker.WorkerRole.DOCTOR, "MBBS");
            worker(kondapur, "Sunita Pawar", HealthcareWorker.WorkerRole.NURSE, "GNM");
            worker(kondapur, "Kavita Jadhav", HealthcareWorker.WorkerRole.ASHA_WORKER, "ASHA training");
            worker(anandpur, "Meena Shinde", HealthcareWorker.WorkerRole.ANM, "ANM");
            worker(anandpur, "Ramesh Gaikwad", HealthcareWorker.WorkerRole.PHARMACIST, "D.Pharm");
            worker(shivnagar, "Dr. M. Iyer", HealthcareWorker.WorkerRole.DOCTOR, "MD (Community Medicine)");
            worker(shivnagar, "Pooja Kale", HealthcareWorker.WorkerRole.LAB_TECHNICIAN, "DMLT");
            worker(rampur, "Asha More", HealthcareWorker.WorkerRole.ASHA_WORKER, "ASHA training");

            for (HealthCentre c : new HealthCentre[]{kondapur, anandpur, shivnagar, rampur}) {
                service(c, "General OPD", "Outpatient consultation");
                service(c, "Immunisation", "Routine childhood and maternal vaccination");
            }
            service(kondapur, "Maternal care", "Antenatal check-ups and counselling");
            service(shivnagar, "Laboratory", "Blood, urine and malaria tests");

            for (DayOfWeek d : DayOfWeek.values()) {
                for (HealthCentre c : new HealthCentre[]{kondapur, anandpur, shivnagar}) {
                    schedule(c, d, d == DayOfWeek.SUNDAY);
                }
            }
            log.info("Seeded demo health centres");
        }

        if (residentRepository.count() == 0) {
            Resident r1 = resident("Lakshmi Bai Patil", "Kondapur", Resident.Gender.FEMALE, 1968, "B+", null, "Hypertension");
            Resident r2 = resident("Ganesh Shinde", "Kondapur", Resident.Gender.MALE, 1985, "O+", null, null);
            Resident r3 = resident("Sarita Kamble", "Anandpur", Resident.Gender.FEMALE, 1992, "A+", "Penicillin", null);
            Resident r4 = resident("Vitthal Jadhav", "Anandpur", Resident.Gender.MALE, 1955, "AB+", null, "Type 2 diabetes");
            resident("Priya Gaikwad", "Shivnagar", Resident.Gender.FEMALE, 2001, "O-", null, null);
            Resident r6 = resident("Mohan Pawar", "Shivnagar", Resident.Gender.MALE, 1978, "B-", null, "Asthma");
            resident("Rekha More", "Rampur", Resident.Gender.FEMALE, 1989, "A-", null, null);
            resident("Suresh Kale", "Rampur", Resident.Gender.MALE, 1963, "O+", "Sulfa drugs", null);
            resident("Anita Deshmukh", "Kondapur", Resident.Gender.FEMALE, 1996, "B+", null, null);
            resident("Rahul Salunkhe", "Anandpur", Resident.Gender.MALE, 2010, "A+", null, null);

            visit(r1, 20, "Blood pressure review", "BP 150/95, continue medication", 1L);
            visit(r1, 3, "Headache and dizziness", "BP 145/92, advised salt reduction", 1L);
            visit(r3, 12, "Fever", "Viral fever, symptomatic treatment", 2L);
            visit(r4, 7, "Diabetes follow-up", "Fasting sugar 160 mg/dL", 2L);
            visit(r6, 5, "Breathlessness", "Mild asthma exacerbation, inhaler technique reviewed", 3L);
            visit(r2, 30, "Minor injury", "Wound dressing", 1L);
            log.info("Seeded demo residents");
        }
    }

    private HealthCentre centre(String name, String village, HealthCentre.CentreType type, String phone) {
        HealthCentre c = new HealthCentre();
        c.setName(name);
        c.setVillage(village);
        c.setCentreType(type);
        c.setContactPhone(phone);
        c.setAddress("Main road, " + village);
        return centreRepository.save(c);
    }

    private void worker(HealthCentre c, String name, HealthcareWorker.WorkerRole role, String qualification) {
        HealthcareWorker w = new HealthcareWorker();
        w.setHealthCentre(c);
        w.setFullName(name);
        w.setRole(role);
        w.setQualification(qualification);
        workerRepository.save(w);
    }

    private void service(HealthCentre c, String name, String description) {
        CentreService s = new CentreService();
        s.setHealthCentre(c);
        s.setServiceName(name);
        s.setDescription(description);
        serviceRepository.save(s);
    }

    private void schedule(HealthCentre c, DayOfWeek day, boolean closed) {
        OperatingSchedule s = new OperatingSchedule();
        s.setHealthCentre(c);
        s.setDayOfWeek(day);
        s.setClosed(closed);
        if (!closed) {
            s.setOpenTime(LocalTime.of(9, 0));
            s.setCloseTime(day == DayOfWeek.SATURDAY ? LocalTime.of(13, 0) : LocalTime.of(17, 0));
        }
        scheduleRepository.save(s);
    }

    private Resident resident(String name, String village, Resident.Gender gender, int birthYear,
                               String blood, String allergies, String conditions) {
        Resident r = new Resident();
        r.setFullName(name);
        r.setVillage(village);
        r.setGender(gender);
        r.setDateOfBirth(LocalDate.of(birthYear, 6, 15));
        r.setBloodGroup(blood);
        r.setKnownAllergies(allergies);
        r.setChronicConditions(conditions);
        r.setHouseholdAddress("Ward " + (name.length() % 5 + 1) + ", " + village);
        r.setContactPhone("98" + String.format("%08d", Math.abs(name.hashCode()) % 100000000));
        return residentRepository.save(r);
    }

    private void visit(Resident r, int daysAgo, String reason, String diagnosis, Long centreId) {
        MedicalRecord m = new MedicalRecord();
        m.setResident(r);
        m.setVisitDate(LocalDate.now().minusDays(daysAgo));
        m.setReasonForVisit(reason);
        m.setDiagnosisSummary(diagnosis);
        m.setHealthCentreId(centreId);
        medicalRecordRepository.save(m);
    }
}
