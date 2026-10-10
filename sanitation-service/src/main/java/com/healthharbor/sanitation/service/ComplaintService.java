package com.healthharbor.sanitation.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.common.dto.StatusUpdateRequest;
import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.sanitation.entity.Complaint;
import com.healthharbor.sanitation.entity.Complaint.ComplaintCategory;
import com.healthharbor.sanitation.entity.Complaint.ComplaintStatus;
import com.healthharbor.sanitation.entity.MaintenanceTask;
import com.healthharbor.sanitation.entity.MaintenanceTask.MaintenanceType;
import com.healthharbor.sanitation.entity.SanitationFacility;
import com.healthharbor.sanitation.repository.ComplaintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ComplaintService extends CrudService<Complaint> {

    private final SanitationFacilityService facilities;
    private final MaintenanceTaskService tasks;

    public ComplaintService(ComplaintRepository repository, SanitationFacilityService facilities, MaintenanceTaskService tasks) {
        super(repository, Complaint.class, "Complaint");
        this.facilities = facilities;
        this.tasks = tasks;
    }

    @Override
    protected void beforeSave(Complaint c, Complaint previous) {
        if (c.getFacilityId() != null) {
            SanitationFacility f = facilities.require(c.getFacilityId());
            c.setFacilityName(f.getName());
            if (c.getVillage() == null || c.getVillage().isBlank()) {
                c.setVillage(f.getVillage());
            }
        }
        if (c.getReportedDate() == null) {
            c.setReportedDate(LocalDate.now());
        }
    }

    @Override
    protected void afterCreate(Complaint c) {
        c.setComplaintNumber(reference("CMP", c.getId()));
        repository.save(c);
    }

    @Override
    protected void onStatusChange(Complaint c, Object oldStatus, Object newStatus, StatusUpdateRequest request) {
        if (newStatus == ComplaintStatus.RESOLVED || newStatus == ComplaintStatus.CLOSED) {
            if (c.getResolvedDate() == null) {
                c.setResolvedDate(LocalDate.now());
            }
        }
        if (request.notes() != null && !request.notes().isBlank()) {
            c.setResolutionNotes(request.notes());
        }
    }

    /** Converts a complaint into a maintenance work order and links them. */
    @Transactional
    public MaintenanceTask convertToTask(Long complaintId, MaintenanceType type, String assignedTo, String by) {
        Complaint c = get(complaintId);
        if (c.getFacilityId() == null) {
            throw new BusinessException("Link the complaint to a facility before creating a maintenance task");
        }
        if (c.getMaintenanceTaskId() != null) {
            throw new BusinessException("Complaint already has maintenance task " + c.getMaintenanceTaskId());
        }
        MaintenanceTask task = new MaintenanceTask();
        task.setFacilityId(c.getFacilityId());
        task.setMaintenanceType(type == null ? defaultType(c.getCategory()) : type);
        task.setDescription(c.getComplaintNumber() + ": " + c.getDescription());
        task.setPriority(c.getPriority());
        task.setSource(MaintenanceTask.TaskSource.COMPLAINT);
        task.setAssignedTo(assignedTo);
        task.setReportedBy(by == null ? c.getComplainantName() : by);
        task.setScheduledDate(LocalDate.now().plusDays(1));
        task = tasks.create(task);
        c.setMaintenanceTaskId(task.getId());
        c.setStatus(ComplaintStatus.IN_PROGRESS);
        c.setAssignedTo(assignedTo);
        repository.save(c);
        return task;
    }

    static MaintenanceType defaultType(ComplaintCategory category) {
        return switch (category) {
            case BLOCKED_DRAIN, SEWAGE_OVERFLOW -> MaintenanceType.DRAIN_DESILTING;
            case NO_WATER -> MaintenanceType.PLUMBING;
            case BROKEN_FIXTURE -> MaintenanceType.REPAIR;
            case NO_LIGHTING -> MaintenanceType.ELECTRICAL;
            default -> MaintenanceType.CLEANING;
        };
    }
}
