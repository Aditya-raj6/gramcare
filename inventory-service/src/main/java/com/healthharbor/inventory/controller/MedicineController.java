package com.healthharbor.inventory.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.inventory.entity.Medicine;
import com.healthharbor.inventory.service.MedicineService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medicines")
@Tag(name = "Medicine")
public class MedicineController extends CrudController<Medicine> {
    public MedicineController(MedicineService service) {
        super(service);
    }
}
