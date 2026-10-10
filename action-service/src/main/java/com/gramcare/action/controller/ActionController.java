package com.gramcare.action.controller;

import com.gramcare.action.dto.*;
import com.gramcare.action.entity.ActionHistory;
import com.gramcare.action.entity.ActionItem;
import com.gramcare.action.service.ActionItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/actions")
public class ActionController {

    private final ActionItemService actionItemService;

    public ActionController(ActionItemService actionItemService) {
        this.actionItemService = actionItemService;
    }

    @GetMapping
    public List<ActionItemResponse> list(@RequestParam(required = false) String status,
                                          @RequestParam(required = false) String category,
                                          @RequestParam(required = false) String village,
                                          @RequestParam(required = false) String priority) {
        return actionItemService.findAll(status, category, village, priority)
                .stream().map(ActionItemResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ActionItemResponse get(@PathVariable Long id) {
        return ActionItemResponse.from(actionItemService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ActionItemResponse> create(@Valid @RequestBody ActionItemRequest req) {
        ActionItem item = actionItemService.createOrGetExisting(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ActionItemResponse.from(item));
    }

    @PutMapping("/{id}/assign")
    public ActionItemResponse assign(@PathVariable Long id, @RequestBody AssignRequest req) {
        return ActionItemResponse.from(actionItemService.assign(id, req));
    }

    @PutMapping("/{id}/status")
    public ActionItemResponse updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest req) {
        return ActionItemResponse.from(actionItemService.updateStatus(id, req));
    }

    @GetMapping("/{id}/history")
    public List<ActionHistory> history(@PathVariable Long id) {
        return actionItemService.getHistory(id);
    }

    @GetMapping("/departments")
    public List<com.gramcare.action.entity.DepartmentAssignment> departments() {
        return actionItemService.getDepartments();
    }

    @GetMapping("/summary")
    public java.util.Map<String, Object> summary() {
        return java.util.Map.of(
                "totalPending", actionItemService.countPending(),
                "critical", actionItemService.findCritical().size()
        );
    }
}
