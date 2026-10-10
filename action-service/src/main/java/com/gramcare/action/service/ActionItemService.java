package com.gramcare.action.service;

import com.gramcare.action.dto.*;
import com.gramcare.action.entity.*;
import com.gramcare.action.exception.ResourceNotFoundException;
import com.gramcare.action.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ActionItemService {

    private final ActionItemRepository actionItemRepository;
    private final ActionHistoryRepository historyRepository;
    private final DepartmentAssignmentRepository departmentAssignmentRepository;

    public ActionItemService(ActionItemRepository actionItemRepository,
                              ActionHistoryRepository historyRepository,
                              DepartmentAssignmentRepository departmentAssignmentRepository) {
        this.actionItemRepository = actionItemRepository;
        this.historyRepository = historyRepository;
        this.departmentAssignmentRepository = departmentAssignmentRepository;
    }

    /**
     * Creates an action item, or returns the existing one unchanged if a
     * record with the same sourceService/sourceReferenceType/sourceReferenceId
     * already exists. This is what makes retried calls from upstream services
     * safe: the same complaint reported twice never produces two action items.
     */
    public ActionItem createOrGetExisting(ActionItemRequest req) {
        Optional<ActionItem> existing = actionItemRepository
                .findBySourceServiceAndSourceReferenceTypeAndSourceReferenceId(
                        req.getSourceService(), req.getSourceReferenceType(), req.getSourceReferenceId());
        if (existing.isPresent()) {
            return existing.get();
        }

        ActionItem item = new ActionItem();
        item.setTitle(req.getTitle());
        item.setDescription(req.getDescription());
        item.setCategory(req.getCategory());
        item.setVillage(req.getVillage());
        item.setHealthCentreId(req.getHealthCentreId());
        if (req.getPriority() != null) item.setPriority(req.getPriority());
        item.setReportedBy(req.getReportedBy());
        item.setDueDate(req.getDueDate());
        item.setSourceService(req.getSourceService());
        item.setSourceReferenceType(req.getSourceReferenceType());
        item.setSourceReferenceId(req.getSourceReferenceId());

        // Routing: send the item to the department responsible for its
        // category. A responsible person is chosen later by the officer, which
        // is what moves the item from OPEN to ASSIGNED.
        departmentAssignmentRepository.findByCategory(req.getCategory())
                .ifPresent(da -> item.setAssignedDepartment(da.getDepartmentName()));

        if (item.getDueDate() == null) {
            item.setDueDate(defaultDueDate(item.getPriority()));
        }

        ActionItem saved = actionItemRepository.save(item);
        recordHistory(saved, null, req.getReportedBy() != null ? req.getReportedBy() : req.getSourceService(),
                "Created from " + req.getSourceService() + " (" + req.getSourceReferenceType() + " #" + req.getSourceReferenceId() + ")"
                        + (saved.getAssignedDepartment() != null ? ", routed to " + saved.getAssignedDepartment() : ""));
        return saved;
    }

    /** Target resolution time by priority, used when the reporter gives no due date. */
    static java.time.LocalDate defaultDueDate(ActionItem.Priority priority) {
        int days = switch (priority) {
            case CRITICAL -> 1;
            case HIGH -> 3;
            case MEDIUM -> 7;
            case LOW -> 14;
        };
        return java.time.LocalDate.now().plusDays(days);
    }

    @Transactional(readOnly = true)
    public List<ActionItem> findAll(String status, String category, String village, String priority) {
        if (status != null && !status.isBlank()) return actionItemRepository.findByStatus(ActionItem.Status.valueOf(status.toUpperCase()));
        if (category != null && !category.isBlank()) return actionItemRepository.findByCategory(ActionItem.Category.valueOf(category.toUpperCase()));
        if (village != null && !village.isBlank()) return actionItemRepository.findByVillageIgnoreCase(village);
        if (priority != null && !priority.isBlank()) return actionItemRepository.findByPriority(ActionItem.Priority.valueOf(priority.toUpperCase()));
        return actionItemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ActionItem findById(Long id) {
        return actionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action item not found with id " + id));
    }

    public ActionItem assign(Long id, AssignRequest req) {
        ActionItem item = findById(id);
        ActionItem.Status previous = item.getStatus();
        item.setAssignedDepartment(req.getAssignedDepartment());
        item.setAssignedPerson(req.getAssignedPerson());
        if (item.getStatus() == ActionItem.Status.OPEN
                && req.getAssignedPerson() != null && !req.getAssignedPerson().isBlank()) {
            item.setStatus(ActionItem.Status.ASSIGNED);
        }
        recordHistory(item, previous, req.getChangedBy(),
                "Assigned to " + req.getAssignedDepartment() + (req.getAssignedPerson() != null ? " / " + req.getAssignedPerson() : ""));
        return actionItemRepository.save(item);
    }

    public ActionItem updateStatus(Long id, StatusUpdateRequest req) {
        if (req.getStatus() == null) {
            throw new IllegalArgumentException("status is required");
        }
        ActionItem item = findById(id);
        ActionItem.Status previous = item.getStatus();
        item.setStatus(req.getStatus());
        if (req.getNotes() != null) item.setResolutionNotes(req.getNotes());
        if (req.getStatus() == ActionItem.Status.RESOLVED || req.getStatus() == ActionItem.Status.CLOSED) {
            if (item.getResolvedDate() == null) item.setResolvedDate(LocalDateTime.now());
        } else {
            item.setResolvedDate(null);
        }
        recordHistory(item, previous, req.getChangedBy(), req.getNotes());
        return actionItemRepository.save(item);
    }

    private void recordHistory(ActionItem item, ActionItem.Status previous, String changedBy, String notes) {
        ActionHistory history = new ActionHistory();
        history.setActionItem(item);
        history.setPreviousStatus(previous);
        history.setNewStatus(item.getStatus());
        history.setChangedBy(changedBy);
        history.setNotes(notes);
        historyRepository.save(history);
        item.getHistory().add(history);
    }

    @Transactional(readOnly = true)
    public List<ActionHistory> getHistory(Long actionItemId) {
        return historyRepository.findByActionItemIdOrderByChangedAtDesc(actionItemId);
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return actionItemRepository.findAll().stream()
                .filter(a -> a.getStatus() != ActionItem.Status.RESOLVED && a.getStatus() != ActionItem.Status.CLOSED)
                .count();
    }

    @Transactional(readOnly = true)
    public List<ActionItem> findCritical() {
        return actionItemRepository.findByPriority(ActionItem.Priority.CRITICAL);
    }

    @Transactional(readOnly = true)
    public List<ActionItem> findRecent(int limit) {
        return actionItemRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentAssignment> getDepartments() {
        return departmentAssignmentRepository.findAll();
    }
}
