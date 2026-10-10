package com.healthharbor.patient.config;

import com.healthharbor.patient.entity.MedicalRecord;
import com.healthharbor.patient.entity.Resident;
import com.healthharbor.patient.entity.Visit;
import com.healthharbor.patient.repository.MedicalRecordRepository;
import com.healthharbor.patient.repository.ResidentRepository;
import com.healthharbor.patient.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.healthharbor.patient.entity.Resident.Gender.FEMALE;
import static com.healthharbor.patient.entity.Resident.Gender.MALE;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "healthharbor.seed-data", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final ResidentRepository residents;
    private final VisitRepository visits;
    private final MedicalRecordRepository records;

    @Override
    @Transactional
    public void run(String... args) {
        if (residents.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now();
        Resident r1 = resident("Ramesh", "Kumar", MALE, "1968-03-12", "Rampur", "B+", "Type 2 Diabetes, Hypertension", null, "H-101");
        Resident r2 = resident("Sita", "Devi", FEMALE, "1996-07-21", "Rampur", "O+", null, null, "H-101");
        r2.setPregnant(true);
        residents.save(r2);
        Resident r3 = resident("Mohan", "Lal", MALE, "1955-11-02", "Sundarpur", "A+", "COPD", "Penicillin", "H-214");
        Resident r4 = resident("Geeta", "Kumari", FEMALE, "2012-01-15", "Sundarpur", "B+", null, null, "H-214");
        Resident r5 = resident("Anil", "Verma", MALE, "1983-09-30", "Kishanganj", "AB+", null, "Sulfa drugs", "H-033");
        Resident r6 = resident("Lakshmi", "Bai", FEMALE, "1972-05-18", "Kishanganj", "O-", "Hypertension", null, "H-045");
        Resident r7 = resident("Suresh", "Yadav", MALE, "1990-12-08", "Devgarh", "B-", null, null, "H-310");
        Resident r8 = resident("Pooja", "Sharma", FEMALE, "2000-04-25", "Devgarh", "A-", "Anaemia", null, "H-311");
        Resident r9 = resident("Kamla", "Devi", FEMALE, "1948-08-14", "Rampur", "O+", "Arthritis, Cataract", null, "H-120");
        Resident r10 = resident("Raju", "Prasad", MALE, "2019-06-03", "Bhimnagar", "B+", null, null, "H-402");
        Resident r11 = resident("Farida", "Begum", FEMALE, "1988-02-27", "Bhimnagar", "A+", "Tuberculosis (on DOTS)", null, "H-405");
        Resident r12 = resident("Vikram", "Singh", MALE, "1976-10-11", "Lakshmipur", "O+", "Type 2 Diabetes", null, "H-501");
        Resident r13 = resident("Asha", "Patel", FEMALE, "1999-09-09", "Lakshmipur", "B+", null, null, "H-502");
        r13.setPregnant(true);
        residents.save(r13);
        Resident r14 = resident("Gopal", "Mishra", MALE, "1962-01-20", "Sundarpur", "AB-", "Hypertension", null, "H-220");

        visit(r1, today.minusDays(2), Visit.VisitType.OPD, 1L, "PHC Rampur", "Routine diabetes review", "Type 2 DM - controlled", "Dr. Arjun Patil", "140/90", 98.4, 78, 72.0);
        visit(r2, today.minusDays(5), Visit.VisitType.ANTENATAL, 1L, "PHC Rampur", "ANC visit - 2nd trimester", "Normal pregnancy", "ANM Kavita Rao", "110/70", 98.2, 84, 58.5);
        visit(r3, today.minusDays(8), Visit.VisitType.OPD, 2L, "Sub-Centre Sundarpur", "Breathlessness", "COPD exacerbation", "Dr. Neha Gupta", "130/85", 99.1, 92, 61.0);
        visit(r4, today.minusDays(12), Visit.VisitType.IMMUNIZATION, 2L, "Sub-Centre Sundarpur", "Td booster", "Immunization given", "ANM Kavita Rao", null, 98.0, 88, 34.0);
        visit(r5, today.minusDays(1), Visit.VisitType.EMERGENCY, 3L, "CHC Kishanganj", "Acute diarrhoea", "Acute gastroenteritis", "Dr. Sameer Khan", "100/60", 100.2, 104, 64.0);
        visit(r6, today.minusDays(15), Visit.VisitType.FOLLOW_UP, 3L, "CHC Kishanganj", "BP follow-up", "Essential hypertension", "Dr. Sameer Khan", "150/95", 98.6, 80, 69.0);
        visit(r8, today.minusDays(4), Visit.VisitType.OPD, 4L, "Sub-Centre Devgarh", "Fatigue, dizziness", "Iron deficiency anaemia", "Dr. Neha Gupta", "100/70", 98.3, 90, 47.0);
        visit(r9, today.minusDays(20), Visit.VisitType.HOME_VISIT, 1L, "PHC Rampur", "Joint pain - home visit", "Osteoarthritis knees", "Sunita Devi (ASHA)", "135/85", 98.1, 76, 52.0);
        visit(r10, today.minusDays(3), Visit.VisitType.OPD, 3L, "CHC Kishanganj", "Fever and cough", "Viral URTI", "Dr. Sameer Khan", null, 101.0, 110, 16.5);
        visit(r11, today.minusDays(6), Visit.VisitType.FOLLOW_UP, 3L, "CHC Kishanganj", "DOTS adherence check", "Pulmonary TB - continuation phase", "Dr. Sameer Khan", "115/75", 98.6, 82, 49.0);
        visit(r12, today.minusDays(9), Visit.VisitType.HEALTH_CAMP, null, "NCD Screening Camp Lakshmipur", "Camp screening", "High blood sugar - referred", "Dr. Arjun Patil", "138/88", 98.4, 79, 81.0);
        visit(r14, today.minusDays(11), Visit.VisitType.OPD, 2L, "Sub-Centre Sundarpur", "Headache", "Uncontrolled hypertension", "Dr. Neha Gupta", "160/100", 98.5, 86, 74.0);
        visit(r1, today.minusDays(35), Visit.VisitType.OPD, 1L, "PHC Rampur", "Diabetes review", "Type 2 DM", "Dr. Arjun Patil", "145/92", 98.6, 80, 73.0);

        record(r1, MedicalRecord.RecordType.LAB_RESULT, "HbA1c report", "HbA1c 7.2% - fair control", today.minusDays(2), "Lab Tech Imran");
        record(r1, MedicalRecord.RecordType.PRESCRIPTION, "Metformin 500mg BD", "Continue Metformin 500mg twice daily, Amlodipine 5mg OD", today.minusDays(2), "Dr. Arjun Patil");
        record(r2, MedicalRecord.RecordType.ANTENATAL, "ANC registration", "LMP recorded; EDD computed; IFA & calcium started", today.minusDays(60), "ANM Kavita Rao");
        record(r3, MedicalRecord.RecordType.ALLERGY, "Penicillin allergy", "Rash and swelling after penicillin in 2019", today.minusYears(5), "Dr. Neha Gupta");
        record(r4, MedicalRecord.RecordType.VACCINATION, "Td booster", "Td dose administered under UIP", today.minusDays(12), "ANM Kavita Rao");
        record(r10, MedicalRecord.RecordType.VACCINATION, "MR 2nd dose", "Measles-Rubella dose 2 completed", today.minusMonths(14), "ANM Kavita Rao");
        record(r11, MedicalRecord.RecordType.DIAGNOSIS, "Pulmonary TB", "Sputum CBNAAT positive; enrolled in Nikshay; DOTS started", today.minusMonths(4), "Dr. Sameer Khan");
        record(r8, MedicalRecord.RecordType.LAB_RESULT, "Haemoglobin", "Hb 8.9 g/dL - moderate anaemia", today.minusDays(4), "Lab Tech Imran");
        log.info("Seeded {} residents, {} visits, {} medical records", residents.count(), visits.count(), records.count());
    }

    private Resident resident(String first, String last, Resident.Gender gender, String dob, String village,
                              String blood, String chronic, String allergies, String household) {
        Resident r = new Resident();
        r.setFirstName(first);
        r.setLastName(last);
        r.setGender(gender);
        r.setDateOfBirth(LocalDate.parse(dob));
        r.setVillage(village);
        r.setAddress("Ward " + (Math.abs(household.hashCode()) % 9 + 1) + ", " + village);
        r.setHouseholdNumber(household);
        r.setBloodGroup(blood);
        r.setChronicConditions(chronic);
        r.setAllergies(allergies);
        r.setPhone("+91 9" + String.format("%09d", Math.abs((first + last).hashCode()) % 1000000000));
        r.setAadhaarLast4(String.format("%04d", Math.abs((last + first).hashCode()) % 10000));
        r.setEmergencyContactName("Family member");
        r.setEmergencyContactPhone("+91 9" + String.format("%09d", Math.abs((last + village).hashCode()) % 1000000000));
        r.setPregnant(false);
        r.setDisability(false);
        r = residents.save(r);
        r.setResidentCode(String.format("RES-%05d", r.getId()));
        return residents.save(r);
    }

    private void visit(Resident r, LocalDate date, Visit.VisitType type, Long centreId, String centre, String reason,
                       String diagnosis, String by, String bp, Double temp, Integer pulse, Double weight) {
        Visit v = new Visit();
        v.setResidentId(r.getId());
        v.setResidentName(r.getFullName());
        v.setVisitDate(date);
        v.setVisitType(type);
        v.setHealthCentreId(centreId);
        v.setHealthCentreName(centre);
        v.setReason(reason);
        v.setDiagnosis(diagnosis);
        v.setAttendedBy(by);
        v.setBloodPressure(bp);
        v.setTemperatureC(temp == null ? null : Math.round((temp - 32) * 5 / 9 * 10) / 10.0);
        v.setPulse(pulse);
        v.setWeightKg(weight);
        visits.save(v);
    }

    private void record(Resident r, MedicalRecord.RecordType type, String title, String description, LocalDate date, String by) {
        MedicalRecord m = new MedicalRecord();
        m.setResidentId(r.getId());
        m.setResidentName(r.getFullName());
        m.setRecordType(type);
        m.setTitle(title);
        m.setDescription(description);
        m.setRecordDate(date);
        m.setRecordedBy(by);
        records.save(m);
    }
}
