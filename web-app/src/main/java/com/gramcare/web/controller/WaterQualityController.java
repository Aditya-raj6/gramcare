package com.gramcare.web.controller;

import com.gramcare.web.client.SanitationClient;
import com.gramcare.web.client.ServiceUnavailableException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/water-quality")
public class WaterQualityController {

    private final SanitationClient sanitationClient;

    public WaterQualityController(SanitationClient sanitationClient) {
        this.sanitationClient = sanitationClient;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String village, Model model) {
        try {
            model.addAttribute("observations", sanitationClient.listObservations(village));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("observations", List.of());
        }
        model.addAttribute("villageFilter", village);
        return "water/list";
    }

    @GetMapping("/new")
    public String newForm() { return "water/form"; }

    @PostMapping
    public String create(@RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("village", form.get("village"));
        body.put("waterSourceName", form.get("waterSourceName"));
        body.put("observationDate", blankToNull(form.get("observationDate")));
        body.put("ph", blankToNull(form.get("ph")));
        body.put("turbidityNtu", blankToNull(form.get("turbidityNtu")));
        body.put("residualChlorineMgL", blankToNull(form.get("residualChlorineMgL")));
        body.put("recordedBy", form.get("recordedBy"));
        body.put("notes", form.get("notes"));
        Map<String, Object> saved = sanitationClient.createObservation(body);
        return "redirect:/water-quality/" + saved.get("id");
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("obs", sanitationClient.getObservation(id));
            model.addAttribute("correctiveActions", sanitationClient.getCorrectiveActions(id));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("correctiveActions", List.of());
        }
        return "water/view";
    }

    @PostMapping("/{id}/corrective-actions")
    public String addCorrectiveAction(@PathVariable Long id, @RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("actionDescription", form.get("actionDescription"));
        body.put("assignedTo", form.get("assignedTo"));
        body.put("dueDate", blankToNull(form.get("dueDate")));
        body.put("followUpNotes", form.get("followUpNotes"));
        sanitationClient.addCorrectiveAction(id, body);
        sanitationClient.updateReviewStatus(id, Map.of("reviewStatus", "ACTION_TAKEN"));
        return "redirect:/water-quality/" + id;
    }

    @PostMapping("/{id}/retry-link")
    public String retryLink(@PathVariable Long id) {
        sanitationClient.retryObservationLink(id);
        return "redirect:/water-quality/" + id;
    }

    @PostMapping("/{id}/review-status")
    public String updateReviewStatus(@PathVariable Long id, @RequestParam String reviewStatus) {
        sanitationClient.updateReviewStatus(id, Map.of("reviewStatus", reviewStatus));
        return "redirect:/water-quality/" + id;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
