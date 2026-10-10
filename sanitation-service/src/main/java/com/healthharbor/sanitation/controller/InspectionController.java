package com.healthharbor.sanitation.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.sanitation.entity.Inspection;
import com.healthharbor.sanitation.service.InspectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sanitation/inspections")
@Tag(name = "Cleanliness inspection")
public class InspectionController extends CrudController<Inspection> {
    public InspectionController(InspectionService service) {
        super(service);
    }
}
