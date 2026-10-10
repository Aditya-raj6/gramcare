package com.healthharbor.sanitation.config;

import com.healthharbor.sanitation.entity.*;
import com.healthharbor.sanitation.entity.Complaint.ComplaintCategory;
import com.healthharbor.sanitation.entity.Complaint.ComplaintStatus;
import com.healthharbor.sanitation.entity.MaintenanceTask.MaintenanceType;
import com.healthharbor.sanitation.entity.MaintenanceTask.TaskStatus;
import com.healthharbor.sanitation.entity.SanitationFacility.CleanlinessStatus;
import com.healthharbor.sanitation.entity.SanitationFacility.FacilityType;
import com.healthharbor.sanitation.entity.SanitationFacility.OperationalStatus;
import com.healthharbor.sanitation.repository.*;
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

    private final SanitationFacilityRepository facilities;
    private final InspectionRepository inspections;
    private final MaintenanceTaskRepository tasks;
    private final ComplaintRepository complaints;

    @Override
    @Transactional
    public void run(String... args) {
        if (facilities.count() > 0) {
            return;
        }
        LocalDate t = LocalDate.now();
        SanitationFacility f1 = facility("Community Sanitary Complex - Rampur Market", FacilityType.COMMUNITY_TOILET, "Rampur", "Ward 3", "Near weekly haat", 12, CleanlinessStatus.CLEAN, OperationalStatus.OPERATIONAL, true, 26.8470, 80.9455);
        SanitationFacility f2 = facility("Public Toilet - Sundarpur Bus Stand", FacilityType.PUBLIC_TOILET, "Sundarpur", "Ward 1", "Bus stand", 6, CleanlinessStatus.DIRTY, OperationalStatus.OPERATIONAL, false, 26.8605, 80.9820);
        SanitationFacility f3 = facility("Main Drainage Line - Kishanganj", FacilityType.DRAINAGE, "Kishanganj", "Ward 5", "Along school road", null, CleanlinessStatus.UNUSABLE, OperationalStatus.NON_FUNCTIONAL, false, 26.8110, 80.9031);
        SanitationFacility f4 = facility("Waste Collection Point - Devgarh Chowk", FacilityType.WASTE_COLLECTION_POINT, "Devgarh", "Ward 2", "Devgarh chowk", null, CleanlinessStatus.SATISFACTORY, OperationalStatus.OPERATIONAL, false, 26.8885, 80.9340);
        SanitationFacility f5 = facility("School Toilet Block - GPS Bhimnagar", FacilityType.SCHOOL_TOILET, "Bhimnagar", "Ward 4", "Govt. Primary School", 8, CleanlinessStatus.SATISFACTORY, OperationalStatus.UNDER_MAINTENANCE, true, 26.8350, 80.9600);
        SanitationFacility f6 = facility("Handwash Station - PHC Rampur", FacilityType.HANDWASH_STATION, "Rampur", "Ward 1", "PHC entrance", 4, CleanlinessStatus.CLEAN, OperationalStatus.OPERATIONAL, true, 26.8467, 80.9462);
        SanitationFacility f7 = facility("Community Soak Pit - Lakshmipur", FacilityType.SOAK_PIT, "Lakshmipur", "Ward 2", "Behind panchayat bhawan", null, CleanlinessStatus.SATISFACTORY, OperationalStatus.OPERATIONAL, false, 26.8720, 80.9150);
        SanitationFacility f8 = facility("Women's Sanitary Complex - Devgarh", FacilityType.COMMUNITY_TOILET, "Devgarh", "Ward 3", "Near Anganwadi", 10, CleanlinessStatus.CLEAN, OperationalStatus.OPERATIONAL, true, 26.8899, 80.9322);

        inspect(f1, t.minusDays(1), "Prakash Yadav", 5, CleanlinessStatus.CLEAN, "Well maintained; soap available");
        inspect(f2, t.minusDays(2), "Prakash Yadav", 2, CleanlinessStatus.DIRTY, "Floor dirty, no water in overhead tank");
        inspect(f3, t.minusDays(3), "Prakash Yadav", 1, CleanlinessStatus.UNUSABLE, "Drain choked with plastic; water logging near school");
        inspect(f4, t.minusDays(5), "Mahesh Valmiki", 3, CleanlinessStatus.SATISFACTORY, "Bins full but collected daily");
        inspect(f8, t.minusDays(4), "Prakash Yadav", 4, CleanlinessStatus.CLEAN, null);

        MaintenanceTask m1 = task(f2, MaintenanceType.CLEANING, "Deep cleaning and restore water supply to overhead tank", Priority.HIGH, TaskStatus.ASSIGNED, MaintenanceTask.TaskSource.INSPECTION, "Mahesh Valmiki", t.plusDays(1));
        MaintenanceTask m2 = task(f3, MaintenanceType.DRAIN_DESILTING, "Desilting of 300m drain and removal of plastic waste", Priority.CRITICAL, TaskStatus.IN_PROGRESS, MaintenanceTask.TaskSource.COMPLAINT, "Gram Panchayat sanitation team", t);
        task(f5, MaintenanceType.PLUMBING, "Replace broken flush tanks and leaking pipes", Priority.MEDIUM, TaskStatus.IN_PROGRESS, MaintenanceTask.TaskSource.MANUAL, "Local plumber - Raju Mistri", t.minusDays(1));
        task(f7, MaintenanceType.DESLUDGING, "Scheduled desludging of soak pit", Priority.LOW, TaskStatus.PENDING, MaintenanceTask.TaskSource.SCHEDULED, null, t.plusDays(6));
        MaintenanceTask done = task(f1, MaintenanceType.PAINTING, "Repaint walls with IEC messages on handwashing", Priority.LOW, TaskStatus.COMPLETED, MaintenanceTask.TaskSource.MANUAL, "SHG Painters", t.minusDays(10));
        done.setCompletedDate(t.minusDays(6));
        done.setActualCost(4500.0);
        tasks.save(done);

        complaint(f3, "Kishanganj", ComplaintCategory.BLOCKED_DRAIN, "Drain overflowing onto school road, mosquito breeding", "Anil Verma", Priority.CRITICAL, ComplaintStatus.IN_PROGRESS, m2.getId(), "CITIZEN");
        complaint(f2, "Sundarpur", ComplaintCategory.NO_WATER, "No water in toilet since 3 days", "Mohan Lal", Priority.HIGH, ComplaintStatus.IN_PROGRESS, m1.getId(), "CITIZEN");
        complaint(f4, "Devgarh", ComplaintCategory.GARBAGE_NOT_COLLECTED, "Garbage not lifted on Sunday, stray animals spreading waste", "Suresh Yadav", Priority.MEDIUM, ComplaintStatus.OPEN, null, "HEALTH_WORKER");
        complaint(null, "Bhimnagar", ComplaintCategory.OPEN_DEFECATION, "Open defecation observed near pond behind ward 4", "Farida Begum", Priority.HIGH, ComplaintStatus.OPEN, null, "HEALTH_WORKER");
        complaint(f1, "Rampur", ComplaintCategory.NO_LIGHTING, "Lights not working at night in women's section", "Sita Devi", Priority.MEDIUM, ComplaintStatus.RESOLVED, null, "CITIZEN");
        log.info("Seeded {} sanitation facilities, {} tasks, {} complaints", facilities.count(), tasks.count(), complaints.count());
    }

    private SanitationFacility facility(String name, FacilityType type, String village, String ward, String landmark, Integer capacity,
                                        CleanlinessStatus clean, OperationalStatus status, boolean accessible, double lat, double lng) {
        SanitationFacility f = new SanitationFacility();
        f.setName(name);
        f.setType(type);
        f.setVillage(village);
        f.setWard(ward);
        f.setLandmark(landmark);
        f.setCapacity(capacity);
        f.setCleanlinessStatus(clean);
        f.setStatus(status);
        f.setAccessibleForDisabled(accessible);
        f.setGenderSeparated(capacity != null && capacity >= 8);
        f.setWaterAvailable(clean != CleanlinessStatus.DIRTY);
        f.setElectricityAvailable(true);
        f.setLatitude(lat);
        f.setLongitude(lng);
        f.setCaretakerName(type == FacilityType.DRAINAGE ? "Gram Panchayat" : "SHG Swachhata Samiti");
        f.setCaretakerPhone("+91 9" + String.format("%09d", Math.abs(name.hashCode()) % 1000000000));
        f.setManagedBy("Gram Panchayat " + village);
        f.setLastCleanedDate(LocalDate.now().minusDays(Math.abs(name.hashCode()) % 7));
        f = facilities.save(f);
        f.setFacilityCode(String.format("SAN-%05d", f.getId()));
        return facilities.save(f);
    }

    private void inspect(SanitationFacility f, LocalDate date, String inspector, int rating, CleanlinessStatus status, String remarks) {
        Inspection i = new Inspection();
        i.setFacilityId(f.getId());
        i.setFacilityName(f.getName());
        i.setVillage(f.getVillage());
        i.setInspectionDate(date);
        i.setInspectorName(inspector);
        i.setRating(rating);
        i.setCleanlinessStatus(status);
        i.setWaterAvailable(rating > 2);
        i.setLightingWorking(rating > 1);
        i.setSoapAvailable(rating >= 4);
        i.setOdourFree(rating >= 3);
        i.setRemarks(remarks);
        inspections.save(i);
        f.setLastInspectionDate(date);
        facilities.save(f);
    }

    private MaintenanceTask task(SanitationFacility f, MaintenanceType type, String description, Priority priority, TaskStatus status,
                                 MaintenanceTask.TaskSource source, String assignedTo, LocalDate scheduled) {
        MaintenanceTask m = new MaintenanceTask();
        m.setFacilityId(f.getId());
        m.setFacilityName(f.getName());
        m.setVillage(f.getVillage());
        m.setMaintenanceType(type);
        m.setDescription(description);
        m.setPriority(priority);
        m.setStatus(status);
        m.setSource(source);
        m.setAssignedTo(assignedTo);
        m.setReportedBy("Prakash Yadav");
        m.setScheduledDate(scheduled);
        m.setEstimatedCost(type == MaintenanceType.DRAIN_DESILTING ? 18000.0 : 3500.0);
        m = tasks.save(m);
        m.setTaskNumber(String.format("MNT-%05d", m.getId()));
        return tasks.save(m);
    }

    private void complaint(SanitationFacility f, String village, ComplaintCategory category, String description, String by,
                           Priority priority, ComplaintStatus status, Long taskId, String channel) {
        Complaint c = new Complaint();
        if (f != null) {
            c.setFacilityId(f.getId());
            c.setFacilityName(f.getName());
        }
        c.setVillage(village);
        c.setCategory(category);
        c.setDescription(description);
        c.setComplainantName(by);
        c.setComplainantPhone("+91 9" + String.format("%09d", Math.abs(by.hashCode()) % 1000000000));
        c.setPriority(priority);
        c.setStatus(status);
        c.setMaintenanceTaskId(taskId);
        c.setChannel(channel);
        c.setReportedDate(LocalDate.now().minusDays(Math.abs(description.hashCode()) % 5 + 1));
        if (status == ComplaintStatus.RESOLVED) {
            c.setResolvedDate(LocalDate.now().minusDays(1));
            c.setResolutionNotes("LED fittings replaced by electrician");
        }
        c = complaints.save(c);
        c.setComplaintNumber(String.format("CMP-%05d", c.getId()));
        complaints.save(c);
    }
}
