package com.healthharbor.water.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.common.dto.StatusUpdateRequest;
import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.water.entity.PublicHealthIssue;
import com.healthharbor.water.entity.PublicHealthIssue.IssueStatus;
import com.healthharbor.water.entity.WaterSource;
import com.healthharbor.water.repository.PublicHealthIssueRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PublicHealthIssueService extends CrudService<PublicHealthIssue> {

    private final WaterSourceService sources;

    public PublicHealthIssueService(PublicHealthIssueRepository repository, WaterSourceService sources) {
        super(repository, PublicHealthIssue.class, "Public health issue");
        this.sources = sources;
    }

    public PublicHealthIssue require(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException("Public health issue " + id + " does not exist"));
    }

    void save(PublicHealthIssue issue) {
        repository.save(issue);
    }

    @Override
    protected void beforeSave(PublicHealthIssue issue, PublicHealthIssue previous) {
        if (issue.getSourceId() != null) {
            WaterSource source = sources.require(issue.getSourceId());
            issue.setSourceName(source.getName());
            if (issue.getVillage() == null || issue.getVillage().isBlank()) {
                issue.setVillage(source.getVillage());
            }
            if (issue.getAffectedPopulation() == null) {
                issue.setAffectedPopulation(source.getPopulationServed());
            }
        }
        if (issue.getReportedDate() == null) {
            issue.setReportedDate(LocalDate.now());
        }
    }

    @Override
    protected void afterCreate(PublicHealthIssue issue) {
        issue.setIssueNumber(reference("PHI", issue.getId()));
        repository.save(issue);
    }

    @Override
    protected void validateTransition(PublicHealthIssue issue, Object oldStatus, Object newStatus) {
        if (oldStatus == IssueStatus.CLOSED && newStatus != IssueStatus.CLOSED) {
            throw new BusinessException("Closed issues cannot be reopened; raise a new issue instead");
        }
    }

    @Override
    protected void onStatusChange(PublicHealthIssue issue, Object oldStatus, Object newStatus, StatusUpdateRequest request) {
        if (request.notes() != null && !request.notes().isBlank()) {
            issue.setResolutionNotes(request.notes());
        }
        if ((newStatus == IssueStatus.RESOLVED || newStatus == IssueStatus.CLOSED) && issue.getResolvedDate() == null) {
            issue.setResolvedDate(LocalDate.now());
            if (issue.getSourceId() != null && issue.getCategory() == PublicHealthIssue.IssueCategory.WATER_CONTAMINATION) {
                WaterSource source = sources.require(issue.getSourceId());
                if (source.getStatus() == WaterSource.SourceStatus.CONTAMINATED) {
                    source.setStatus(WaterSource.SourceStatus.FUNCTIONAL);
                    sources.save(source);
                }
            }
        }
    }
}
