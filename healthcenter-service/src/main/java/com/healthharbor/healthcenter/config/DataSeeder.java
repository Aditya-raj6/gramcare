package com.healthharbor.healthcenter.config;

import com.healthharbor.healthcenter.entity.*;
import com.healthharbor.healthcenter.entity.Facility.FacilityCategory;
import com.healthharbor.healthcenter.entity.HealthWorker.WorkerRole;
import com.healthharbor.healthcenter.entity.ServiceOffering.ServiceCategory;
import com.healthharbor.healthcenter.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "healthharbor.seed-data", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final HealthCentreRepository centres;
    private final FacilityRepository facilities;
    private final HealthWorkerRepository workers;
    private final ServiceOfferingRepository services;
    private final OperatingScheduleRepository schedules;

    @Override
    @Transactional
    public void run(String... args) {
        if (centres.count() > 0) {
            return;
        }
        HealthCentre rampur = centre("HC-00001", "PHC Rampur", HealthCentre.CentreType.PHC, "Rampur", "Dr. Arjun Patil", 10, 30000, 26.8467, 80.9462);
        HealthCentre sundarpur = centre("HC-00002", "Sub-Centre Sundarpur", HealthCentre.CentreType.SUB_CENTRE, "Sundarpur", "ANM Kavita Rao", 2, 5000, 26.8612, 80.9811);
        HealthCentre kishanganj = centre("HC-00003", "CHC Kishanganj", HealthCentre.CentreType.CHC, "Kishanganj", "Dr. Sameer Khan", 30, 120000, 26.8105, 80.9020);
        HealthCentre devgarh = centre("HC-00004", "Sub-Centre Devgarh", HealthCentre.CentreType.SUB_CENTRE, "Devgarh", "Dr. Neha Gupta", 2, 4500, 26.8890, 80.9335);
        devgarh.setStatus(HealthCentre.CentreStatus.LIMITED);
        centres.save(devgarh);

        worker(rampur, "Dr. Arjun Patil", WorkerRole.DOCTOR, "General Medicine", "MBBS, MD");
        worker(rampur, "Sunita Devi", WorkerRole.ASHA, null, "ASHA Certified");
        worker(rampur, "Ravi Kumar", WorkerRole.PHARMACIST, null, "D.Pharm");
        worker(rampur, "Imran Ali", WorkerRole.LAB_TECHNICIAN, null, "DMLT");
        worker(sundarpur, "Kavita Rao", WorkerRole.ANM, "Maternal & Child Health", "ANM Diploma");
        worker(sundarpur, "Dinesh Chauhan", WorkerRole.MPW, null, "MPW Training");
        worker(kishanganj, "Dr. Sameer Khan", WorkerRole.DOCTOR, "Paediatrics", "MBBS, DCH");
        worker(kishanganj, "Dr. Priya Nair", WorkerRole.DOCTOR, "Obstetrics & Gynaecology", "MBBS, MS (OBG)");
        worker(kishanganj, "Mary Joseph", WorkerRole.NURSE, "Staff Nurse", "B.Sc Nursing");
        HealthWorker neha = worker(devgarh, "Dr. Neha Gupta", WorkerRole.DOCTOR, "Family Medicine", "MBBS");
        HealthWorker rekha = worker(devgarh, "Rekha Kumari", WorkerRole.ASHA, null, "ASHA Certified");
        rekha.setStatus(HealthWorker.WorkerStatus.ON_LEAVE);
        workers.save(rekha);
        worker(devgarh, "Mahesh Valmiki", WorkerRole.SANITATION_WORKER, null, null);

        facility(rampur, "Clinical Laboratory", FacilityCategory.LABORATORY, 1, Facility.FacilityStatus.AVAILABLE, "CBC, blood sugar, malaria RDT, urine");
        facility(rampur, "Pharmacy Counter", FacilityCategory.PHARMACY, 1, Facility.FacilityStatus.AVAILABLE, null);
        facility(rampur, "Inpatient Beds", FacilityCategory.INPATIENT_BEDS, 10, Facility.FacilityStatus.AVAILABLE, null);
        facility(rampur, "ILR Cold Chain", FacilityCategory.VACCINATION_COLD_CHAIN, 2, Facility.FacilityStatus.AVAILABLE, null);
        facility(rampur, "108 Ambulance", FacilityCategory.AMBULANCE, 1, Facility.FacilityStatus.UNDER_MAINTENANCE, "Gearbox repair at district workshop");
        facility(sundarpur, "Labour Room", FacilityCategory.LABOUR_ROOM, 1, Facility.FacilityStatus.AVAILABLE, null);
        facility(sundarpur, "Telemedicine Kiosk", FacilityCategory.TELEMEDICINE, 1, Facility.FacilityStatus.AVAILABLE, "eSanjeevani hub link");
        facility(kishanganj, "Digital X-Ray", FacilityCategory.X_RAY, 1, Facility.FacilityStatus.AVAILABLE, null);
        facility(kishanganj, "Operation Theatre", FacilityCategory.OPERATION_THEATRE, 1, Facility.FacilityStatus.AVAILABLE, null);
        facility(kishanganj, "Inpatient Beds", FacilityCategory.INPATIENT_BEDS, 30, Facility.FacilityStatus.AVAILABLE, null);
        facility(kishanganj, "Solar Power Backup", FacilityCategory.POWER_BACKUP, 1, Facility.FacilityStatus.AVAILABLE, null);
        facility(devgarh, "RO Drinking Water", FacilityCategory.DRINKING_WATER, 1, Facility.FacilityStatus.UNAVAILABLE, "RO membrane failed - awaiting replacement");
        facility(devgarh, "Patient Toilets", FacilityCategory.TOILETS, 2, Facility.FacilityStatus.AVAILABLE, null);

        service(rampur, "General OPD", ServiceCategory.GENERAL_OPD, "Mon - Sat");
        service(rampur, "Routine Immunization", ServiceCategory.IMMUNIZATION, "Wed, Sat");
        service(rampur, "NCD Clinic (Diabetes/Hypertension)", ServiceCategory.NCD, "Tue, Fri");
        service(rampur, "Basic Lab Diagnostics", ServiceCategory.DIAGNOSTICS, "Mon - Sat");
        service(sundarpur, "Antenatal Care", ServiceCategory.MATERNAL_HEALTH, "Thu");
        service(sundarpur, "Family Planning Counselling", ServiceCategory.FAMILY_PLANNING, "Mon, Thu");
        service(kishanganj, "24x7 Emergency", ServiceCategory.EMERGENCY, "All days");
        service(kishanganj, "TB DOTS Centre", ServiceCategory.COMMUNICABLE_DISEASE, "Mon - Sat");
        service(kishanganj, "Paediatric OPD", ServiceCategory.CHILD_HEALTH, "Mon - Sat");
        service(kishanganj, "Dental Clinic", ServiceCategory.DENTAL, "Tue, Thu, Sat");
        service(devgarh, "General OPD", ServiceCategory.GENERAL_OPD, "Mon, Wed, Fri");

        for (HealthCentre c : new HealthCentre[]{rampur, sundarpur, kishanganj, devgarh}) {
            for (DayOfWeek d : DayOfWeek.values()) {
                OperatingSchedule s = new OperatingSchedule();
                s.setCentreId(c.getId());
                s.setCentreName(c.getName());
                s.setDayOfWeek(d);
                boolean is24x7 = c == kishanganj;
                s.setClosed(!is24x7 && d == DayOfWeek.SUNDAY);
                s.setOpenTime(is24x7 ? LocalTime.MIDNIGHT : LocalTime.of(9, 0));
                s.setCloseTime(is24x7 ? LocalTime.of(23, 59) : (d == DayOfWeek.SATURDAY ? LocalTime.of(13, 0) : LocalTime.of(16, 0)));
                s.setNotes(is24x7 ? "24x7 emergency" : null);
                schedules.save(s);
            }
        }
        OperatingSchedule duty = new OperatingSchedule();
        duty.setCentreId(devgarh.getId());
        duty.setCentreName(devgarh.getName());
        duty.setWorkerId(neha.getId());
        duty.setWorkerName(neha.getName());
        duty.setDayOfWeek(DayOfWeek.WEDNESDAY);
        duty.setOpenTime(LocalTime.of(10, 0));
        duty.setCloseTime(LocalTime.of(14, 0));
        duty.setClosed(false);
        duty.setNotes("Visiting doctor duty");
        schedules.save(duty);
        log.info("Seeded {} health centres, {} workers, {} facilities", centres.count(), workers.count(), facilities.count());
    }

    private HealthCentre centre(String code, String name, HealthCentre.CentreType type, String village, String inCharge,
                                int beds, int population, double lat, double lng) {
        HealthCentre c = new HealthCentre();
        c.setCode(code);
        c.setName(name);
        c.setType(type);
        c.setVillage(village);
        c.setBlock("Rampur Block");
        c.setDistrict("Anandpur");
        c.setAddress("Main Road, " + village);
        c.setPhone("+91 5" + String.format("%09d", Math.abs(name.hashCode()) % 1000000000));
        c.setEmail(name.toLowerCase().replaceAll("[^a-z]", "") + "@health.gov.in");
        c.setInChargeName(inCharge);
        c.setBedCapacity(beds);
        c.setPopulationCovered(population);
        c.setLatitude(lat);
        c.setLongitude(lng);
        return centres.save(c);
    }

    private HealthWorker worker(HealthCentre c, String name, WorkerRole role, String specialization, String qualification) {
        HealthWorker w = new HealthWorker();
        w.setCentreId(c.getId());
        w.setCentreName(c.getName());
        w.setName(name);
        w.setRole(role);
        w.setSpecialization(specialization);
        w.setQualification(qualification);
        w.setPhone("+91 9" + String.format("%09d", Math.abs(name.hashCode()) % 1000000000));
        w.setJoiningDate(LocalDate.now().minusMonths(Math.abs(name.hashCode()) % 60 + 3));
        w.setVillagesCovered(c.getVillage());
        return workers.save(w);
    }

    private void facility(HealthCentre c, String name, FacilityCategory category, int qty, Facility.FacilityStatus status, String notes) {
        Facility f = new Facility();
        f.setCentreId(c.getId());
        f.setCentreName(c.getName());
        f.setName(name);
        f.setCategory(category);
        f.setQuantity(qty);
        f.setStatus(status);
        f.setNotes(notes);
        facilities.save(f);
    }

    private void service(HealthCentre c, String name, ServiceCategory category, String days) {
        ServiceOffering s = new ServiceOffering();
        s.setCentreId(c.getId());
        s.setCentreName(c.getName());
        s.setName(name);
        s.setCategory(category);
        s.setAvailableDays(days);
        s.setFee(0.0);
        s.setAvailable(true);
        services.save(s);
    }
}
