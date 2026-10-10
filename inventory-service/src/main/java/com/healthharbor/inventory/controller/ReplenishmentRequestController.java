package com.healthharbor.inventory.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.inventory.entity.ReplenishmentRequest;
import com.healthharbor.inventory.service.ReplenishmentRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/replenishments")
@Tag(name = "Replenishment request")
public class ReplenishmentRequestController extends CrudController<ReplenishmentRequest> {
    public ReplenishmentRequestController(ReplenishmentRequestService service) {
        super(service);
    }
}
