package com.healthharbor.healthcenter.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.healthcenter.entity.OperatingSchedule;
import com.healthharbor.healthcenter.service.OperatingScheduleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
@Tag(name = "Operating Schedule")
public class OperatingScheduleController extends CrudController<OperatingSchedule> {
    public OperatingScheduleController(OperatingScheduleService service) {
        super(service);
    }
}
