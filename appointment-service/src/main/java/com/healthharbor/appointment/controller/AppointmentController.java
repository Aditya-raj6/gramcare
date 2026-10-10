package com.healthharbor.appointment.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.appointment.entity.Appointment;
import com.healthharbor.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Appointment")
public class AppointmentController extends CrudController<Appointment> {
    public AppointmentController(AppointmentService service) {
        super(service);
    }
}
