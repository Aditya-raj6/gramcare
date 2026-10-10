package com.healthharbor.healthcamp.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.healthcamp.entity.CampStaff;
import com.healthharbor.healthcamp.service.CampStaffService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/camp-staff")
@Tag(name = "Camp staff")
public class CampStaffController extends CrudController<CampStaff> {
    public CampStaffController(CampStaffService service) {
        super(service);
    }
}
