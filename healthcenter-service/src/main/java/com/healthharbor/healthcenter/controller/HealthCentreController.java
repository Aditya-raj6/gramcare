package com.healthharbor.healthcenter.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.healthcenter.entity.HealthCentre;
import com.healthharbor.healthcenter.service.HealthCentreService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health-centres")
@Tag(name = "Health Centre")
public class HealthCentreController extends CrudController<HealthCentre> {
    public HealthCentreController(HealthCentreService service) {
        super(service);
    }
}
