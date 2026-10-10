package com.healthharbor.healthcamp.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.healthcamp.entity.Beneficiary;
import com.healthharbor.healthcamp.service.BeneficiaryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/beneficiaries")
@Tag(name = "Beneficiary")
public class BeneficiaryController extends CrudController<Beneficiary> {
    public BeneficiaryController(BeneficiaryService service) {
        super(service);
    }
}
