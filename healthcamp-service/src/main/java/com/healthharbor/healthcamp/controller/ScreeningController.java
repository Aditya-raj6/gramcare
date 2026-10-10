package com.healthharbor.healthcamp.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.healthcamp.entity.Screening;
import com.healthharbor.healthcamp.service.ScreeningService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/screenings")
@Tag(name = "Screening")
public class ScreeningController extends CrudController<Screening> {
    public ScreeningController(ScreeningService service) {
        super(service);
    }
}
