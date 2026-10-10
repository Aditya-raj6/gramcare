package com.healthharbor.sanitation.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.sanitation.entity.SanitationFacility;
import com.healthharbor.sanitation.service.SanitationFacilityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sanitation/facilities")
@Tag(name = "Sanitation facility")
public class SanitationFacilityController extends CrudController<SanitationFacility> {
    public SanitationFacilityController(SanitationFacilityService service) {
        super(service);
    }
}
