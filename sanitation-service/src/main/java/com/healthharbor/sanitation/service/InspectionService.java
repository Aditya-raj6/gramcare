package com.healthharbor.sanitation.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.sanitation.entity.Inspection;
import com.healthharbor.sanitation.entity.MaintenanceTask;
import com.healthharbor.sanitation.entity.Priority;
import com.healthharbor.sanitation.entity.SanitationFacility;
import com.healthharbor.sanitation.entity.SanitationFacility.CleanlinessStatus;
import com.healthharbor.sanitation.repository.InspectionRepository;
import com.healthharbor.sanitation.repository.MaintenanceTaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/** Inspections refresh the facility's cleanliness and auto-raise cleaning work for poor results. */
@Service
public class InspectionService extends CrudService<Inspection> {

    private final SanitationFacilityService facilities;
    private final MaintenanceTaskService taskService;
    private final MaintenanceTaskRepository tasks;

    public InspectionService(InspectionRepository repository, SanitationFacilityService facilities,
                             MaintenanceTaskService taskService, MaintenanceTaskRepository tasks) {
        super(repository, Inspection.class, "Inspection");
        this.facilities = facilities;
        this.taskService = taskService;
        this.tasks = tasks;
    }

    @Override
    protected void beforeSave(Inspection inspection, Inspection previous) {
        SanitationFacility facility = facilities.require(inspection.getFacilityId());
        inspection.setFacilityName(facility.getName());
        inspection.setVillage(facility.getVillage());
        if (inspection.getInspectionDate() == null) {
            inspection.setInspectionDate(LocalDate.now());
        }
    }

    @Override
    protected void afterCreate(Inspection inspection) {
        SanitationFacility facility = facilities.require(inspection.getFacilityId());
        facility.setCleanlinessStatus(inspection.getCleanlinessStatus());
        facility.setLastInspectionDate(inspection.getInspectionDate());
        if (inspection.getWaterAvailable() != null) {
            facility.setWaterAvailable(inspection.getWaterAvailable());
        }
        facilities.save(facility);

        boolean poor = inspection.getCleanlinessStatus() == CleanlinessStatus.UNUSABLE
                || inspection.getCleanlinessStatus() == CleanlinessStatus.DIRTY || inspection.getRating() <= 2;
        boolean alreadyQueued = tasks.existsByFacilityIdAndMaintenanceTypeAndStatusIn(facility.getId(),
                MaintenanceTask.MaintenanceType.CLEANING, MaintenanceTaskService.ACTIVE);
        if (poor && !alreadyQueued) {
            MaintenanceTask task = new MaintenanceTask();
            task.setFacilityId(facility.getId());
            task.setMaintenanceType(MaintenanceTask.MaintenanceType.CLEANING);
            task.setDescription("Deep cleaning required after inspection rated " + inspection.getRating() + "/5 ("
                    + inspection.getCleanlinessStatus() + ")" + (inspection.getRemarks() == null ? "" : ": " + inspection.getRemarks()));
            task.setPriority(inspection.getCleanlinessStatus() == CleanlinessStatus.UNUSABLE ? Priority.CRITICAL : Priority.HIGH);
            task.setSource(MaintenanceTask.TaskSource.INSPECTION);
            task.setReportedBy(inspection.getInspectorName());
            task.setScheduledDate(LocalDate.now().plusDays(1));
            task = taskService.create(task);
            inspection.setMaintenanceTaskId(task.getId());
            repository.save(inspection);
        }
    }
}
