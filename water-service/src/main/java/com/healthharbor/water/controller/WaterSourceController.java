package com.healthharbor.water.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.water.entity.WaterSource;
import com.healthharbor.water.service.WaterSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/water/sources")
@Tag(name = "Water source")
public class WaterSourceController extends CrudController<WaterSource> {
    public WaterSourceController(WaterSourceService service) {
        super(service);
    }
}
