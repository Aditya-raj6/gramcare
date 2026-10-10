package com.healthharbor.patient.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.patient.entity.Visit;
import com.healthharbor.patient.service.VisitService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visits")
@Tag(name = "Visit")
public class VisitController extends CrudController<Visit> {
    public VisitController(VisitService service) {
        super(service);
    }
}
