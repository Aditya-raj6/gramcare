package com.healthharbor.sanitation.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.sanitation.entity.MaintenanceTask;
import com.healthharbor.sanitation.service.MaintenanceTaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sanitation/maintenance")
@Tag(name = "Maintenance task")
public class MaintenanceTaskController extends CrudController<MaintenanceTask> {
    public MaintenanceTaskController(MaintenanceTaskService service) {
        super(service);
    }
}
