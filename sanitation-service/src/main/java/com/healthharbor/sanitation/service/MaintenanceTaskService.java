package com.healthharbor.sanitation.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.common.dto.StatusUpdateRequest;
import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.sanitation.entity.Complaint;
import com.healthharbor.sanitation.entity.MaintenanceTask;
import com.healthharbor.sanitation.entity.MaintenanceTask.MaintenanceType;
import com.healthharbor.sanitation.entity.MaintenanceTask.TaskStatus;
import com.healthharbor.sanitation.entity.SanitationFacility;
import com.healthharbor.sanitation.repository.ComplaintRepository;
import com.healthharbor.sanitation.repository.MaintenanceTaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Maintenance workflow PENDING → ASSIGNED → IN_PROGRESS → COMPLETED. Starting structural work marks the
 * facility UNDER_MAINTENANCE; completion restores it, refreshes cleanliness and resolves linked complaints.
 */
@Service
public class MaintenanceTaskService extends CrudService<MaintenanceTask> {

    static final Set<TaskStatus> ACTIVE = EnumSet.of(TaskStatus.PENDING, TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS);
    private static final Set<MaintenanceType> STRUCTURAL = EnumSet.of(MaintenanceType.REPAIR, MaintenanceType.PLUMBING,
            MaintenanceType.ELECTRICAL, MaintenanceType.DESLUDGING, MaintenanceType.CONSTRUCTION, MaintenanceType.DRAIN_DESILTING);
    private static final Map<TaskStatus, Set<TaskStatus>> TRANSITIONS = Map.of(
            TaskStatus.PENDING, EnumSet.of(TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED),
            TaskStatus.ASSIGNED, EnumSet.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED),
            TaskStatus.IN_PROGRESS, EnumSet.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED),
            TaskStatus.COMPLETED, EnumSet.noneOf(TaskStatus.class),
            TaskStatus.CANCELLED, EnumSet.of(TaskStatus.PENDING));

    private final SanitationFacilityService facilities;
    private final MaintenanceTaskRepository tasks;
    private final ComplaintRepository complaints;

    public MaintenanceTaskService(MaintenanceTaskRepository repository, SanitationFacilityService facilities,
                                  ComplaintRepository complaints) {
        super(repository, MaintenanceTask.class, "Maintenance task");
        this.tasks = repository;
        this.facilities = facilities;
        this.complaints = complaints;
    }

    @Override
    protected void beforeSave(MaintenanceTask task, MaintenanceTask previous) {
        SanitationFacility facility = facilities.require(task.getFacilityId());
        task.setFacilityName(facility.getName());
        task.setVillage(facility.getVillage());
        if (previous == null && task.getAssignedTo() != null && !task.getAssignedTo().isBlank() && task.getStatus() == TaskStatus.PENDING) {
            task.setStatus(TaskStatus.ASSIGNED);
        }
    }

    @Override
    protected void afterCreate(MaintenanceTask task) {
        task.setTaskNumber(reference("MNT", task.getId()));
        repository.save(task);
    }

    @Override
    protected void validateTransition(MaintenanceTask task, Object oldStatus, Object newStatus) {
        if (oldStatus != newStatus && !TRANSITIONS.get((TaskStatus) oldStatus).contains((TaskStatus) newStatus)) {
            throw new BusinessException("Cannot move task from " + oldStatus + " to " + newStatus);
        }
    }

    @Override
    protected void onStatusChange(MaintenanceTask task, Object oldStatus, Object newStatus, StatusUpdateRequest request) {
        SanitationFacility facility = facilities.require(task.getFacilityId());
        if (newStatus == TaskStatus.IN_PROGRESS && STRUCTURAL.contains(task.getMaintenanceType())) {
            facility.setStatus(SanitationFacility.OperationalStatus.UNDER_MAINTENANCE);
        }
        if (newStatus == TaskStatus.COMPLETED) {
            task.setCompletedDate(LocalDate.now());
            if (request.notes() != null) {
                task.setCompletionNotes(request.notes());
            }
            boolean otherActive = tasks.existsByFacilityIdAndStatusAndIdNot(task.getFacilityId(), TaskStatus.IN_PROGRESS, task.getId());
            if (!otherActive) {
                facility.setStatus(SanitationFacility.OperationalStatus.OPERATIONAL);
            }
            if (task.getMaintenanceType() == MaintenanceType.CLEANING || task.getMaintenanceType() == MaintenanceType.DESLUDGING) {
                facility.setCleanlinessStatus(SanitationFacility.CleanlinessStatus.CLEAN);
                facility.setLastCleanedDate(LocalDate.now());
            }
            for (Complaint c : complaints.findByMaintenanceTaskId(task.getId())) {
                if (c.getStatus() == Complaint.ComplaintStatus.OPEN || c.getStatus() == Complaint.ComplaintStatus.IN_PROGRESS) {
                    c.setStatus(Complaint.ComplaintStatus.RESOLVED);
                    c.setResolvedDate(LocalDate.now());
                    c.setResolutionNotes("Resolved via " + task.getTaskNumber()
                            + (request.notes() == null ? "" : ": " + request.notes()));
                    complaints.save(c);
                }
            }
        }
        facilities.save(facility);
    }
}
