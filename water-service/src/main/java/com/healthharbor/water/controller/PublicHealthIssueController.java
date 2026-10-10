package com.healthharbor.water.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.water.entity.PublicHealthIssue;
import com.healthharbor.water.service.PublicHealthIssueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/water/issues")
@Tag(name = "Public health issue")
public class PublicHealthIssueController extends CrudController<PublicHealthIssue> {
    public PublicHealthIssueController(PublicHealthIssueService service) {
        super(service);
    }
}
