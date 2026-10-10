package com.healthharbor.patient.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.patient.entity.MedicalRecord;
import com.healthharbor.patient.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medical-records")
@Tag(name = "Medical Record")
public class MedicalRecordController extends CrudController<MedicalRecord> {
    public MedicalRecordController(MedicalRecordService service) {
        super(service);
    }
}
