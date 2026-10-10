package com.healthharbor.healthcenter.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.healthcenter.entity.HealthWorker;
import com.healthharbor.healthcenter.service.HealthWorkerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health-workers")
@Tag(name = "Health Worker")
public class HealthWorkerController extends CrudController<HealthWorker> {
    public HealthWorkerController(HealthWorkerService service) {
        super(service);
    }
}
