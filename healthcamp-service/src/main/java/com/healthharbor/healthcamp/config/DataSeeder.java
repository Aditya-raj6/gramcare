package com.healthharbor.healthcamp.config;

import com.healthharbor.healthcamp.entity.*;
import com.healthharbor.healthcamp.entity.HealthCamp.CampStatus;
import com.healthharbor.healthcamp.entity.HealthCamp.CampType;
import com.healthharbor.healthcamp.entity.Screening.Outcome;
import com.healthharbor.healthcamp.entity.Screening.ScreeningType;
import com.healthharbor.healthcamp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "healthharbor.seed-data", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final HealthCampRepository camps;
    private final CampStaffRepository staff;
    private final BeneficiaryRepository beneficiaries;
    private final ScreeningRepository screenings;
    private final FollowUpRepository followUps;

    @Override
    @Transactional
    public void run(String... args) {
        if (camps.count() > 0) {
            return;
        }
        LocalDate t = LocalDate.now();
        HealthCamp ncd = camp("NCD Screening Camp Lakshmipur", CampType.NCD_SCREENING, "Lakshmipur", "Panchayat Bhawan",
                t.minusDays(9), t.minusDays(9), 1L, "PHC Rampur", 150, CampStatus.COMPLETED,
                "Screening for diabetes, hypertension and oral cancer for adults 30+");
        ncd.setOutcomeSummary("132 adults screened; 21 referred to PHC for confirmation");
        camps.save(ncd);
        HealthCamp mch = camp("Mother & Child Health Day - Sundarpur", CampType.MATERNAL_CHILD_HEALTH, "Sundarpur", "Anganwadi Centre 2",
                t, t, 2L, "Sub-Centre Sundarpur", 60, CampStatus.ONGOING, "ANC check-ups, growth monitoring and immunisation");
        HealthCamp eye = camp("Free Eye Camp Rampur", CampType.EYE_CARE, "Rampur", "Govt. Senior Secondary School",
                t.plusDays(4), t.plusDays(5), 1L, "PHC Rampur", 200, CampStatus.PLANNED, "Cataract screening with district hospital ophthalmology team");
        camp("TB Active Case Finding - Bhimnagar", CampType.TB_SCREENING, "Bhimnagar", "Community Hall",
                t.plusDays(12), t.plusDays(13), 3L, "CHC Kishanganj", 300, CampStatus.PLANNED, "Door-to-door symptom screening and sputum collection");

        crew(ncd, "Dr. Arjun Patil", "Doctor", "Consultation & referral", 1L);
        crew(ncd, "Imran Ali", "Lab Technician", "Blood sugar testing", 4L);
        crew(ncd, "Sunita Devi", "ASHA", "Registration & mobilisation", 2L);
        crew(mch, "Kavita Rao", "ANM", "ANC check-ups & immunisation", 5L);
        crew(mch, "Dinesh Chauhan", "MPW", "Growth monitoring", 6L);
        crew(eye, "Dr. R. Menon", "Ophthalmologist", "Eye examination", null);
        crew(eye, "NSS Volunteers (4)", "Volunteer", "Crowd management", null).setVolunteer(true);

        Beneficiary b1 = person(ncd, 12L, "Vikram Singh", 50, Beneficiary.Gender.MALE, "Lakshmipur");
        Beneficiary b2 = person(ncd, null, "Shanti Devi", 58, Beneficiary.Gender.FEMALE, "Lakshmipur");
        Beneficiary b3 = person(ncd, null, "Harish Chandra", 64, Beneficiary.Gender.MALE, "Lakshmipur");
        Beneficiary b4 = person(ncd, 13L, "Asha Patel", 27, Beneficiary.Gender.FEMALE, "Lakshmipur");
        Beneficiary b5 = person(mch, 2L, "Sita Devi", 30, Beneficiary.Gender.FEMALE, "Rampur");
        Beneficiary b6 = person(mch, 4L, "Geeta Kumari", 14, Beneficiary.Gender.FEMALE, "Sundarpur");
        person(mch, null, "Baby of Radha", 1, Beneficiary.Gender.MALE, "Sundarpur");

        screen(b1, ScreeningType.BLOOD_SUGAR, "238 mg/dL (random)", Outcome.ABNORMAL, true, "PHC Rampur", "Imran Ali", t.minusDays(9));
        screen(b1, ScreeningType.BLOOD_PRESSURE, "138/88 mmHg", Outcome.NORMAL, false, null, "Dr. Arjun Patil", t.minusDays(9));
        screen(b2, ScreeningType.BLOOD_PRESSURE, "172/104 mmHg", Outcome.CRITICAL, true, "CHC Kishanganj", "Dr. Arjun Patil", t.minusDays(9));
        screen(b3, ScreeningType.ORAL_CANCER, "White patch left buccal mucosa", Outcome.ABNORMAL, true, "District Hospital", "Dr. Arjun Patil", t.minusDays(9));
        screen(b4, ScreeningType.HAEMOGLOBIN, "10.8 g/dL", Outcome.ABNORMAL, false, null, "Imran Ali", t.minusDays(9));
        screen(b5, ScreeningType.BLOOD_PRESSURE, "112/72 mmHg", Outcome.NORMAL, false, null, "Kavita Rao", t);
        screen(b6, ScreeningType.BMI, "15.9 (underweight)", Outcome.ABNORMAL, true, "Sub-Centre Sundarpur", "Dinesh Chauhan", t);

        follow(b1, "Confirm diabetes with fasting sugar & HbA1c at PHC Rampur", t.minusDays(2), FollowUp.Priority.HIGH, FollowUp.FollowUpStatus.COMPLETED, "Dr. Arjun Patil");
        follow(b2, "Hypertension management at CHC Kishanganj", t.minusDays(7), FollowUp.Priority.CRITICAL, FollowUp.FollowUpStatus.IN_PROGRESS, "Sunita Devi");
        follow(b3, "Biopsy referral at District Hospital", t.minusDays(1), FollowUp.Priority.HIGH, FollowUp.FollowUpStatus.PENDING, "Sunita Devi");
        follow(b6, "Nutrition counselling & deworming", t.plusDays(7), FollowUp.Priority.MEDIUM, FollowUp.FollowUpStatus.PENDING, "Kavita Rao");
        log.info("Seeded {} camps, {} beneficiaries, {} screenings", camps.count(), beneficiaries.count(), screenings.count());
    }

    private HealthCamp camp(String name, CampType type, String village, String venue, LocalDate start, LocalDate end, Long centreId,
                            String centre, int target, CampStatus status, String description) {
        HealthCamp c = new HealthCamp();
        c.setName(name);
        c.setCampType(type);
        c.setVillage(village);
        c.setVenue(venue);
        c.setStartDate(start);
        c.setEndDate(end);
        c.setOrganizer("Block Health Office, Rampur");
        c.setHealthCentreId(centreId);
        c.setHealthCentreName(centre);
        c.setTargetBeneficiaries(target);
        c.setBudget(target * 120.0);
        c.setStatus(status);
        c.setDescription(description);
        c = camps.save(c);
        c.setCampCode(String.format("CAMP-%05d", c.getId()));
        return camps.save(c);
    }

    private CampStaff crew(HealthCamp camp, String name, String role, String assignment, Long workerId) {
        CampStaff s = new CampStaff();
        s.setCampId(camp.getId());
        s.setCampName(camp.getName());
        s.setName(name);
        s.setRole(role);
        s.setAssignment(assignment);
        s.setWorkerId(workerId);
        s.setPhone("+91 9" + String.format("%09d", Math.abs(name.hashCode()) % 1000000000));
        return staff.save(s);
    }

    private Beneficiary person(HealthCamp camp, Long residentId, String name, int age, Beneficiary.Gender gender, String village) {
        Beneficiary b = new Beneficiary();
        b.setCampId(camp.getId());
        b.setCampName(camp.getName());
        b.setResidentId(residentId);
        b.setName(name);
        b.setAge(age);
        b.setGender(gender);
        b.setVillage(village);
        b.setRegisteredOn(camp.getStartDate());
        return beneficiaries.save(b);
    }

    private void screen(Beneficiary b, ScreeningType type, String value, Outcome outcome, boolean referred, String to, String by, LocalDate date) {
        Screening s = new Screening();
        s.setCampId(b.getCampId());
        s.setCampName(b.getCampName());
        s.setBeneficiaryId(b.getId());
        s.setBeneficiaryName(b.getName());
        s.setScreeningType(type);
        s.setResultValue(value);
        s.setOutcome(outcome);
        s.setReferred(referred);
        s.setReferredTo(to);
        s.setScreenedBy(by);
        s.setScreeningDate(date);
        screenings.save(s);
    }

    private void follow(Beneficiary b, String action, LocalDate due, FollowUp.Priority priority, FollowUp.FollowUpStatus status, String to) {
        FollowUp f = new FollowUp();
        f.setCampId(b.getCampId());
        f.setCampName(b.getCampName());
        f.setBeneficiaryId(b.getId());
        f.setBeneficiaryName(b.getName());
        f.setVillage(b.getVillage());
        f.setAction(action);
        f.setDueDate(due);
        f.setPriority(priority);
        f.setStatus(status);
        f.setAssignedTo(to);
        if (status == FollowUp.FollowUpStatus.COMPLETED) {
            f.setCompletedDate(due);
            f.setOutcomeNotes("HbA1c 8.1% - started on Metformin");
        }
        followUps.save(f);
    }
}
