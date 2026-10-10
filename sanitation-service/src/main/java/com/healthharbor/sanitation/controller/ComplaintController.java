package com.healthharbor.sanitation.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.sanitation.entity.Complaint;
import com.healthharbor.sanitation.entity.MaintenanceTask;
import com.healthharbor.sanitation.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sanitation/complaints")
@Tag(name = "Sanitation complaint")
public class ComplaintController extends CrudController<Complaint> {

    private final ComplaintService complaints;

    public ComplaintController(ComplaintService service) {
        super(service);
        this.complaints = service;
    }

    public record ConvertRequest(MaintenanceTask.MaintenanceType maintenanceType, String assignedTo) {
    }

    @PostMapping("/{id}/convert-to-task")
    @Operation(summary = "Create a maintenance work order from this complaint")
    public MaintenanceTask convert(@PathVariable Long id, @RequestBody(required = false) ConvertRequest request,
                                   @RequestHeader(value = "X-User-FullName", required = false) String user) {
        return complaints.convertToTask(id, request == null ? null : request.maintenanceType(),
                request == null ? null : request.assignedTo(), user);
    }
}
