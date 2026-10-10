package com.healthharbor.water.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.water.entity.WaterQualityTest;
import com.healthharbor.water.service.WaterQualityTestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/water/tests")
@Tag(name = "Water quality test")
public class WaterQualityTestController extends CrudController<WaterQualityTest> {
    public WaterQualityTestController(WaterQualityTestService service) {
        super(service);
    }
}
