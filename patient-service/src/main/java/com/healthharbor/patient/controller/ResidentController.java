package com.healthharbor.patient.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.patient.entity.Resident;
import com.healthharbor.patient.service.ResidentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/residents")
@Tag(name = "Resident")
public class ResidentController extends CrudController<Resident> {
    public ResidentController(ResidentService service) {
        super(service);
    }
}
