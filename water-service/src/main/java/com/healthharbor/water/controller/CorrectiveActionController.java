package com.healthharbor.water.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.water.entity.CorrectiveAction;
import com.healthharbor.water.service.CorrectiveActionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/water/actions")
@Tag(name = "Corrective action")
public class CorrectiveActionController extends CrudController<CorrectiveAction> {
    public CorrectiveActionController(CorrectiveActionService service) {
        super(service);
    }
}
