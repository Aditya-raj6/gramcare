package com.healthharbor.appointment.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.appointment.entity.Consultation;
import com.healthharbor.appointment.service.ConsultationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consultations")
@Tag(name = "Consultation")
public class ConsultationController extends CrudController<Consultation> {
    public ConsultationController(ConsultationService service) {
        super(service);
    }
}
