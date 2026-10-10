package com.gramcare.sanitation.controller;

import com.gramcare.sanitation.dto.WaterObservationRequest;
import com.gramcare.sanitation.entity.CorrectiveAction;
import com.gramcare.sanitation.entity.WaterQualityObservation;
import com.gramcare.sanitation.service.SanitationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/water-quality")
public class WaterQualityController {

    private final SanitationService sanitationService;

    public WaterQualityController(SanitationService sanitationService) {
        this.sanitationService = sanitationService;
    }

    @GetMapping("/observations")
    public List<WaterQualityObservation> list(@RequestParam(required = false) String village,
                                               @RequestParam(required = false) String reviewStatus) {
        if (reviewStatus != null && !reviewStatus.isBlank()) {
            return sanitationService.findObservationsByReviewStatus(reviewStatus);
        }
        return sanitationService.findObservations(village);
    }

    @PutMapping("/observations/{id}/review-status")
    public WaterQualityObservation updateReviewStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        return sanitationService.updateReviewStatus(id, body.get("reviewStatus"));
    }

    @PostMapping("/observations/{id}/retry-action-link")
    public WaterQualityObservation retryActionLink(@PathVariable Long id) {
        return sanitationService.retryObservationLink(id);
    }

    @GetMapping("/observations/{id}")
    public WaterQualityObservation get(@PathVariable Long id) { return sanitationService.findObservation(id); }

    @PostMapping("/observations")
    public ResponseEntity<WaterQualityObservation> create(@RequestBody WaterObservationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sanitationService.recordObservation(req));
    }

    @PostMapping("/observations/{id}/corrective-actions")
    public ResponseEntity<CorrectiveAction> addCorrectiveAction(@PathVariable Long id, @RequestBody CorrectiveAction action) {
        action.setWaterQualityObservationId(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(sanitationService.addCorrectiveAction(action));
    }

    @GetMapping("/observations/{id}/corrective-actions")
    public List<CorrectiveAction> getCorrectiveActions(@PathVariable Long id) {
        return sanitationService.getCorrectiveActionsForObservation(id);
    }
}
