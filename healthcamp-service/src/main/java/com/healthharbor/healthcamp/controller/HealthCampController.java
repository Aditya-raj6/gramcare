package com.healthharbor.healthcamp.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.healthcamp.entity.HealthCamp;
import com.healthharbor.healthcamp.service.HealthCampService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health-camps")
@Tag(name = "Health camp")
public class HealthCampController extends CrudController<HealthCamp> {
    public HealthCampController(HealthCampService service) {
        super(service);
    }
}
