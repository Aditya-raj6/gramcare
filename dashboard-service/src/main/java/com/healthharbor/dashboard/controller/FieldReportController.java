package com.healthharbor.dashboard.controller;

import com.healthharbor.dashboard.entity.FieldReport;
import com.healthharbor.dashboard.service.FieldReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/field-reports")
@RequiredArgsConstructor
@Tag(name = "Field reports")
public class FieldReportController {

    private final FieldReportService service;

    @GetMapping
    @Operation(summary = "List field reports with live status of every routed item")
    public List<FieldReport> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public FieldReport get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a field report; each item is routed to its owning service")
    public FieldReport submit(@Valid @RequestBody FieldReport report,
                              @RequestHeader(value = "X-User-FullName", required = false) String user,
                              @RequestHeader(value = "X-User-Role", required = false) String role) {
        return service.submit(report, user, role);
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry routing of failed items")
    public FieldReport retry(@PathVariable Long id) {
        return service.retry(id);
    }
}
