package com.gramcare.sanitation.config;

import com.gramcare.sanitation.entity.*;
import com.gramcare.sanitation.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Demo facilities, inspections, complaints and water observations, inserted
 * only when the facility table is empty. Complaints and flagged observations
 * are saved without an action link; ActionLinkReconciler creates the linked
 * action items once the Action Tracking Service is reachable - the same
 * recovery path used when that service is down during real use.
 */
@Component
@Profile("!test")
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final SanitationFacilityRepository facilityRepository;
    private final CleanlinessInspectionRepository inspectionRepository;
    private final SanitationComplaintRepository complaintRepository;
    private final WaterQualityObservationRepository waterRepository;

    public DemoDataSeeder(SanitationFacilityRepository facilityRepository, CleanlinessInspectionRepository inspectionRepository,
                           SanitationComplaintRepository complaintRepository, WaterQualityObservationRepository waterRepository) {
        this.facilityRepository = facilityRepository;
        this.inspectionRepository = inspectionRepository;
        this.complaintRepository = complaintRepository;
        this.waterRepository = waterRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (facilityRepository.count() > 0) return;

        SanitationFacility t1 = facility("Kondapur Bus Stand Public Toilet", SanitationFacility.FacilityType.PUBLIC_TOILET, "Kondapur", "Near bus stand", SanitationFacility.CleanlinessStatus.POOR);
        SanitationFacility t2 = facility("Anandpur Community Toilet Block", SanitationFacility.FacilityType.COMMUNITY_TOILET_BLOCK, "Anandpur", "Behind Gram Panchayat", SanitationFacility.CleanlinessStatus.SATISFACTORY);
        facility("Shivnagar Water Tank", SanitationFacility.FacilityType.WATER_SUPPLY_POINT, "Shivnagar", "Ward 2 overhead tank", SanitationFacility.CleanlinessStatus.SATISFACTORY);
        SanitationFacility d1 = facility("Shivnagar Main Drain", SanitationFacility.FacilityType.DRAINAGE, "Shivnagar", "Market road", SanitationFacility.CleanlinessStatus.NEEDS_ATTENTION);
        facility("Rampur Waste Collection Point", SanitationFacility.FacilityType.WASTE_COLLECTION_POINT, "Rampur", "Near school", SanitationFacility.CleanlinessStatus.EXCELLENT);

        inspection(t1, 9, "R. Patil", SanitationFacility.CleanlinessStatus.NEEDS_ATTENTION, "Water supply irregular");
        inspection(t1, 2, "R. Patil", SanitationFacility.CleanlinessStatus.POOR, "Broken door latch, no water in cistern");
        inspection(t2, 4, "R. Patil", SanitationFacility.CleanlinessStatus.SATISFACTORY, "Cleaned daily");

        complaint(t1, "Public toilet requires maintenance", "Door latch broken and cistern not filling. Residents avoiding use.",
                "Kavita Jadhav (ASHA)", SanitationComplaint.Priority.HIGH, SanitationComplaint.ComplaintStatus.OPEN);
        complaint(d1, "Blocked drain causing stagnant water", "Drain on market road blocked; mosquito breeding observed.",
                "Pooja Kale", SanitationComplaint.Priority.CRITICAL, SanitationComplaint.ComplaintStatus.IN_PROGRESS);
        complaint(t2, "Lighting not working at night", "Two bulbs fused at the women's section.",
                "Meena Shinde (ANM)", SanitationComplaint.Priority.MEDIUM, SanitationComplaint.ComplaintStatus.OPEN);

        water("Kondapur", "Hand pump 3", 6, "7.4", "2.1", "0.4");
        water("Anandpur", "Open well, ward 1", 3, "6.1", "9.5", "0.0");
        water("Shivnagar", "Overhead tank outlet", 2, "7.9", "3.0", "0.1");
        water("Rampur", "Borewell near school", 1, "7.2", "1.2", "0.3");

        log.info("Seeded demo sanitation and water-quality data");
    }

    private SanitationFacility facility(String name, SanitationFacility.FacilityType type, String village, String location,
                                         SanitationFacility.CleanlinessStatus status) {
        SanitationFacility f = new SanitationFacility();
        f.setFacilityName(name);
        f.setFacilityType(type);
        f.setVillage(village);
        f.setLocation(location);
        f.setCleanlinessStatus(status);
        return facilityRepository.save(f);
    }

    private void inspection(SanitationFacility f, int daysAgo, String by, SanitationFacility.CleanlinessStatus status, String remarks) {
        CleanlinessInspection i = new CleanlinessInspection();
        i.setFacility(f);
        i.setInspectionDate(LocalDate.now().minusDays(daysAgo));
        i.setInspectedBy(by);
        i.setObservedStatus(status);
        i.setRemarks(remarks);
        inspectionRepository.save(i);
    }

    private void complaint(SanitationFacility f, String title, String description, String reportedBy,
                            SanitationComplaint.Priority priority, SanitationComplaint.ComplaintStatus status) {
        SanitationComplaint c = new SanitationComplaint();
        c.setFacility(f);
        c.setTitle(title);
        c.setDescription(description);
        c.setReportedBy(reportedBy);
        c.setPriority(priority);
        c.setStatus(status);
        if (status == SanitationComplaint.ComplaintStatus.IN_PROGRESS) c.setAssignedWorker("Sanitation crew B");
        complaintRepository.save(c);
    }

    private void water(String village, String source, int daysAgo, String ph, String turbidity, String chlorine) {
        WaterQualityObservation o = new WaterQualityObservation();
        o.setVillage(village);
        o.setWaterSourceName(source);
        o.setObservationDate(LocalDate.now().minusDays(daysAgo));
        o.setPh(new BigDecimal(ph));
        o.setTurbidityNtu(new BigDecimal(turbidity));
        o.setResidualChlorineMgL(new BigDecimal(chlorine));
        o.setRecordedBy("A. Deshmukh");
        if (o.isOutOfRange()) o.setReviewStatus(WaterQualityObservation.ReviewStatus.FLAGGED_FOR_REVIEW);
        waterRepository.save(o);
    }
}
