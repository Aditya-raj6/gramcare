package com.healthharbor.healthcenter.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.healthcenter.entity.ServiceOffering;
import com.healthharbor.healthcenter.service.ServiceOfferingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/centre-services")
@Tag(name = "Centre Service")
public class ServiceOfferingController extends CrudController<ServiceOffering> {
    public ServiceOfferingController(ServiceOfferingService service) {
        super(service);
    }
}
