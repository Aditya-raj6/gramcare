package com.healthharbor.water.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.water.entity.PublicHealthIssue;
import com.healthharbor.water.entity.WaterQualityTest;
import com.healthharbor.water.entity.WaterSource;
import com.healthharbor.water.entity.WaterSource.QualityStatus;
import com.healthharbor.water.repository.PublicHealthIssueRepository;
import com.healthharbor.water.repository.WaterQualityTestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;

/**
 * Evaluates each sample; an UNSAFE result flags the source as contaminated and opens a
 * public-health issue (unless one is already open for that source).
 */
@Service
public class WaterQualityTestService extends CrudService<WaterQualityTest> {

    private final WaterSourceService sources;
    private final WaterQualityEvaluator evaluator;
    private final PublicHealthIssueService issueService;
    private final PublicHealthIssueRepository issues;

    public WaterQualityTestService(WaterQualityTestRepository repository, WaterSourceService sources, WaterQualityEvaluator evaluator,
                                   PublicHealthIssueService issueService, PublicHealthIssueRepository issues) {
        super(repository, WaterQualityTest.class, "Water quality test");
        this.sources = sources;
        this.evaluator = evaluator;
        this.issueService = issueService;
        this.issues = issues;
    }

    @Override
    protected void beforeSave(WaterQualityTest test, WaterQualityTest previous) {
        WaterSource source = sources.require(test.getSourceId());
        test.setSourceName(source.getName());
        test.setVillage(source.getVillage());
        if (test.getTestDate() == null) {
            test.setTestDate(LocalDate.now());
        }
        WaterQualityEvaluator.Evaluation evaluation = evaluator.evaluate(test);
        test.setOverallStatus(evaluation.status());
        test.setFindings(String.join("; ", evaluation.findings()));
    }

    @Override
    protected void afterCreate(WaterQualityTest test) {
        WaterSource source = sources.require(test.getSourceId());
        if (source.getLastTestedDate() == null || !test.getTestDate().isBefore(source.getLastTestedDate())) {
            source.setLastTestedDate(test.getTestDate());
            source.setLastQualityStatus(test.getOverallStatus());
        }
        if (test.getOverallStatus() == QualityStatus.UNSAFE) {
            source.setStatus(WaterSource.SourceStatus.CONTAMINATED);
            boolean open = issues.existsBySourceIdAndCategoryAndStatusIn(source.getId(),
                    PublicHealthIssue.IssueCategory.WATER_CONTAMINATION,
                    EnumSet.of(PublicHealthIssue.IssueStatus.OPEN, PublicHealthIssue.IssueStatus.INVESTIGATING,
                            PublicHealthIssue.IssueStatus.ACTION_TAKEN));
            if (!open) {
                PublicHealthIssue issue = new PublicHealthIssue();
                issue.setSourceId(source.getId());
                issue.setVillage(source.getVillage());
                issue.setCategory(PublicHealthIssue.IssueCategory.WATER_CONTAMINATION);
                issue.setTitle("Unsafe water at " + source.getName());
                issue.setDescription(test.getFindings());
                issue.setSeverity(Boolean.TRUE.equals(test.getEcoliPresent()) ? PublicHealthIssue.Severity.CRITICAL : PublicHealthIssue.Severity.HIGH);
                issue.setReportedBy(test.getTestedBy());
                issue.setSource(PublicHealthIssue.IssueSource.QUALITY_TEST);
                issue = issueService.create(issue);
                test.setIssueId(issue.getId());
                repository.save(test);
            }
        }
        sources.save(source);
    }
}
