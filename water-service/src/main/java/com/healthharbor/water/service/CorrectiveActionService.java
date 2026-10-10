package com.healthharbor.water.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.common.dto.StatusUpdateRequest;
import com.healthharbor.water.entity.CorrectiveAction;
import com.healthharbor.water.entity.CorrectiveAction.ActionStatus;
import com.healthharbor.water.entity.PublicHealthIssue;
import com.healthharbor.water.entity.PublicHealthIssue.IssueStatus;
import com.healthharbor.water.entity.WaterSource;
import com.healthharbor.water.repository.CorrectiveActionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CorrectiveActionService extends CrudService<CorrectiveAction> {

    private final PublicHealthIssueService issues;
    private final WaterSourceService sources;

    public CorrectiveActionService(CorrectiveActionRepository repository, PublicHealthIssueService issues, WaterSourceService sources) {
        super(repository, CorrectiveAction.class, "Corrective action");
        this.issues = issues;
        this.sources = sources;
    }

    @Override
    protected void beforeSave(CorrectiveAction action, CorrectiveAction previous) {
        PublicHealthIssue issue = issues.require(action.getIssueId());
        action.setIssueNumber(issue.getIssueNumber());
        action.setVillage(issue.getVillage());
        if (action.getPlannedDate() == null) {
            action.setPlannedDate(LocalDate.now());
        }
    }

    @Override
    protected void afterCreate(CorrectiveAction action) {
        PublicHealthIssue issue = issues.require(action.getIssueId());
        if (issue.getStatus() == IssueStatus.OPEN) {
            issue.setStatus(IssueStatus.INVESTIGATING);
            issues.save(issue);
        }
    }

    @Override
    protected void onStatusChange(CorrectiveAction action, Object oldStatus, Object newStatus, StatusUpdateRequest request) {
        if (request.notes() != null && !request.notes().isBlank()) {
            action.setOutcome(request.notes());
        }
        if (newStatus == ActionStatus.COMPLETED) {
            action.setCompletedDate(LocalDate.now());
            PublicHealthIssue issue = issues.require(action.getIssueId());
            if (issue.getStatus() == IssueStatus.OPEN || issue.getStatus() == IssueStatus.INVESTIGATING) {
                issue.setStatus(IssueStatus.ACTION_TAKEN);
                issues.save(issue);
            }
            if (action.getActionType() == CorrectiveAction.ActionType.CHLORINATION && issue.getSourceId() != null) {
                WaterSource source = sources.require(issue.getSourceId());
                source.setLastChlorinatedDate(LocalDate.now());
                sources.save(source);
            }
        }
    }
}
