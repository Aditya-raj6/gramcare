package com.healthharbor.healthcenter.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.healthcenter.entity.Facility;
import com.healthharbor.healthcenter.service.FacilityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/facilities")
@Tag(name = "Facility")
public class FacilityController extends CrudController<Facility> {
    public FacilityController(FacilityService service) {
        super(service);
    }
}
