package com.healthharbor.water.config;

import com.healthharbor.water.entity.*;
import com.healthharbor.water.entity.CorrectiveAction.ActionStatus;
import com.healthharbor.water.entity.CorrectiveAction.ActionType;
import com.healthharbor.water.entity.PublicHealthIssue.IssueCategory;
import com.healthharbor.water.entity.PublicHealthIssue.IssueStatus;
import com.healthharbor.water.entity.PublicHealthIssue.Severity;
import com.healthharbor.water.entity.WaterSource.SourceStatus;
import com.healthharbor.water.entity.WaterSource.SourceType;
import com.healthharbor.water.repository.*;
import com.healthharbor.water.service.WaterQualityEvaluator;
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

    private final WaterSourceRepository sources;
    private final WaterQualityTestRepository tests;
    private final PublicHealthIssueRepository issues;
    private final CorrectiveActionRepository actions;
    private final WaterQualityEvaluator evaluator;

    @Override
    @Transactional
    public void run(String... args) {
        if (sources.count() > 0) {
            return;
        }
        LocalDate t = LocalDate.now();
        WaterSource s1 = source("Rampur Overhead Tank", SourceType.OVERHEAD_TANK, "Rampur", 420, 2100, "06:00-08:00 & 17:00-19:00", 4.0, SourceStatus.FUNCTIONAL);
        WaterSource s2 = source("Sundarpur Handpump #3", SourceType.HANDPUMP, "Sundarpur", 35, 180, "Round the clock", 24.0, SourceStatus.FUNCTIONAL);
        WaterSource s3 = source("Kishanganj Open Well", SourceType.OPEN_WELL, "Kishanganj", 60, 300, "Round the clock", 24.0, SourceStatus.CONTAMINATED);
        WaterSource s4 = source("Devgarh Borewell", SourceType.BOREWELL, "Devgarh", 110, 560, "05:30-09:00", 3.5, SourceStatus.PARTIALLY_FUNCTIONAL);
        WaterSource s5 = source("Bhimnagar Piped Supply (JJM)", SourceType.PIPED_SUPPLY, "Bhimnagar", 280, 1400, "07:00-09:00", 2.0, SourceStatus.FUNCTIONAL);
        WaterSource s6 = source("Lakshmipur RO Plant", SourceType.RO_PLANT, "Lakshmipur", 200, 950, "08:00-18:00", 10.0, SourceStatus.FUNCTIONAL);
        source("Sundarpur Handpump #7", SourceType.HANDPUMP, "Sundarpur", 25, 130, "Round the clock", 24.0, SourceStatus.NON_FUNCTIONAL);

        test(s1, t.minusDays(3), 7.4, 0.8, 380.0, 0.4, 0.6, 18.0, false, false, "Imran Ali");
        test(s2, t.minusDays(5), 7.1, 2.4, 640.0, null, 1.2, 30.0, false, false, "Prakash Yadav");
        WaterQualityTest bad = test(s3, t.minusDays(2), 6.8, 8.5, 820.0, 0.0, 0.9, 52.0, true, true, "Prakash Yadav");
        test(s4, t.minusDays(8), 7.9, 1.0, 1450.0, null, 1.7, 22.0, false, false, "Imran Ali");
        test(s5, t.minusDays(1), 7.2, 0.5, 310.0, 0.3, 0.5, 12.0, false, false, "Imran Ali");
        test(s6, t.minusDays(6), 7.0, 0.2, 90.0, null, 0.3, 4.0, false, false, "Prakash Yadav");
        test(s3, t.minusDays(40), 7.0, 3.0, 700.0, null, 0.8, 35.0, false, false, "Prakash Yadav");

        PublicHealthIssue i1 = issue(s3, "Kishanganj", IssueCategory.WATER_CONTAMINATION, "Unsafe water at Kishanganj Open Well",
                bad.getFindings(), Severity.CRITICAL, IssueStatus.INVESTIGATING, PublicHealthIssue.IssueSource.QUALITY_TEST, 300, 6);
        bad.setIssueId(i1.getId());
        tests.save(bad);
        PublicHealthIssue i2 = issue(s4, "Devgarh", IssueCategory.SUPPLY_DISRUPTION, "Borewell motor burnt - supply reduced to 3.5 hrs",
                "Motor winding burnt after voltage fluctuation; residents walking 1.5 km to alternate source", Severity.HIGH, IssueStatus.OPEN,
                PublicHealthIssue.IssueSource.MANUAL, 560, 0);
        issue(null, "Bhimnagar", IssueCategory.MOSQUITO_BREEDING, "Stagnant water & larvae near ward 4 pond",
                "Larvae found during entomological survey; 3 fever cases reported", Severity.MEDIUM, IssueStatus.ACTION_TAKEN,
                PublicHealthIssue.IssueSource.SURVEILLANCE, 400, 3);
        issue(s5, "Bhimnagar", IssueCategory.PIPELINE_LEAKAGE, "Pipeline leakage at school junction",
                "Leak repaired by PHED", Severity.LOW, IssueStatus.RESOLVED, PublicHealthIssue.IssueSource.MANUAL, 150, 0);

        action(i1, ActionType.SOURCE_CLOSURE, "Well sealed and marked unsafe; residents informed", "Gram Panchayat", t.minusDays(2), ActionStatus.COMPLETED);
        action(i1, ActionType.CHLORINATION, "Super-chlorination of well with bleaching powder", "Prakash Yadav", t.plusDays(1), ActionStatus.PLANNED);
        action(i1, ActionType.ALTERNATE_SUPPLY, "Daily water tanker until retest is safe", "Block PHED", t.minusDays(1), ActionStatus.IN_PROGRESS);
        action(i1, ActionType.ORS_DISTRIBUTION, "ORS & zinc distribution door-to-door by ASHA", "Sunita Devi", t.minusDays(1), ActionStatus.IN_PROGRESS);
        action(i2, ActionType.HANDPUMP_REPAIR, "Rewind / replace borewell motor", "PHED Mechanic", t.minusDays(1), ActionStatus.PLANNED);
        log.info("Seeded {} water sources, {} quality tests, {} public-health issues", sources.count(), tests.count(), issues.count());
    }

    private WaterSource source(String name, SourceType type, String village, int households, int population, String schedule,
                               double hours, SourceStatus status) {
        WaterSource s = new WaterSource();
        s.setName(name);
        s.setType(type);
        s.setVillage(village);
        s.setWard("Ward " + (Math.abs(name.hashCode()) % 6 + 1));
        s.setHouseholdsServed(households);
        s.setPopulationServed(population);
        s.setSupplySchedule(schedule);
        s.setSupplyHoursPerDay(hours);
        s.setDailyCapacityLitres(population * 55.0);
        s.setManagedBy(type == SourceType.PIPED_SUPPLY ? "PHED / Jal Jeevan Mission" : "Gram Panchayat " + village);
        s.setStatus(status);
        s.setLastChlorinatedDate(LocalDate.now().minusDays(Math.abs(name.hashCode()) % 30));
        s = sources.save(s);
        s.setSourceCode(String.format("WS-%05d", s.getId()));
        return sources.save(s);
    }

    private WaterQualityTest test(WaterSource s, LocalDate date, double ph, double turbidity, double tds, Double chlorine,
                                  double fluoride, double nitrate, boolean coliform, boolean ecoli, String by) {
        WaterQualityTest t = new WaterQualityTest();
        t.setSourceId(s.getId());
        t.setSourceName(s.getName());
        t.setVillage(s.getVillage());
        t.setTestDate(date);
        t.setTestedBy(by);
        t.setMethod(ecoli ? WaterQualityTest.TestMethod.LABORATORY : WaterQualityTest.TestMethod.FIELD_TEST_KIT);
        t.setPh(ph);
        t.setTurbidity(turbidity);
        t.setTds(tds);
        t.setResidualChlorine(chlorine);
        t.setFluoride(fluoride);
        t.setNitrate(nitrate);
        t.setColiformPresent(coliform);
        t.setEcoliPresent(ecoli);
        WaterQualityEvaluator.Evaluation e = evaluator.evaluate(t);
        t.setOverallStatus(e.status());
        t.setFindings(String.join("; ", e.findings()));
        t = tests.save(t);
        if (s.getLastTestedDate() == null || date.isAfter(s.getLastTestedDate())) {
            s.setLastTestedDate(date);
            s.setLastQualityStatus(e.status());
            sources.save(s);
        }
        return t;
    }

    private PublicHealthIssue issue(WaterSource s, String village, IssueCategory category, String title, String description,
                                    Severity severity, IssueStatus status, PublicHealthIssue.IssueSource source, int affected, int cases) {
        PublicHealthIssue i = new PublicHealthIssue();
        if (s != null) {
            i.setSourceId(s.getId());
            i.setSourceName(s.getName());
        }
        i.setVillage(village);
        i.setCategory(category);
        i.setTitle(title);
        i.setDescription(description);
        i.setSeverity(severity);
        i.setStatus(status);
        i.setSource(source);
        i.setAffectedPopulation(affected);
        i.setSuspectedCases(cases);
        i.setReportedBy("Prakash Yadav");
        i.setAssignedTo("Block Sanitation Officer");
        i.setReportedDate(LocalDate.now().minusDays(Math.abs(title.hashCode()) % 6 + 1));
        if (status == IssueStatus.RESOLVED) {
            i.setResolvedDate(LocalDate.now().minusDays(1));
        }
        i = issues.save(i);
        i.setIssueNumber(String.format("PHI-%05d", i.getId()));
        return issues.save(i);
    }

    private void action(PublicHealthIssue issue, ActionType type, String description, String assignedTo, LocalDate planned, ActionStatus status) {
        CorrectiveAction a = new CorrectiveAction();
        a.setIssueId(issue.getId());
        a.setIssueNumber(issue.getIssueNumber());
        a.setVillage(issue.getVillage());
        a.setActionType(type);
        a.setDescription(description);
        a.setAssignedTo(assignedTo);
        a.setPlannedDate(planned);
        a.setStatus(status);
        if (status == ActionStatus.COMPLETED) {
            a.setCompletedDate(planned);
            a.setOutcome("Completed as planned");
        }
        actions.save(a);
    }
}
