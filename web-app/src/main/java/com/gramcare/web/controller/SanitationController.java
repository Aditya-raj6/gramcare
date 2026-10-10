package com.gramcare.web.controller;

import com.gramcare.web.client.SanitationClient;
import com.gramcare.web.client.LenientMap;
import com.gramcare.web.client.ServiceUnavailableException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/sanitation")
public class SanitationController {

    private final SanitationClient sanitationClient;

    public SanitationController(SanitationClient sanitationClient) {
        this.sanitationClient = sanitationClient;
    }

    @GetMapping
    public String overview(Model model) {
        try {
            model.addAttribute("facilities", sanitationClient.listFacilities());
            model.addAttribute("complaints", sanitationClient.listComplaints(null));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("facilities", List.of());
            model.addAttribute("complaints", List.of());
        }
        return "sanitation/overview";
    }

    @GetMapping("/facilities/new")
    public String newFacilityForm() { return "sanitation/facility-form"; }

    @PostMapping("/facilities")
    public String createFacility(@RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("facilityName", form.get("facilityName"));
        body.put("facilityType", form.get("facilityType"));
        body.put("village", form.get("village"));
        body.put("location", form.get("location"));
        sanitationClient.createFacility(body);
        return "redirect:/sanitation";
    }

    @GetMapping("/facilities/{id}")
    public String viewFacility(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("facility", sanitationClient.getFacility(id));
            model.addAttribute("inspections", sanitationClient.getInspections(id));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("facility", new LenientMap());
            model.addAttribute("inspections", List.of());
        }
        return "sanitation/facility-view";
    }

    @PostMapping("/facilities/{id}/inspections")
    public String addInspection(@PathVariable Long id, @RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("inspectionDate", form.get("inspectionDate"));
        body.put("inspectedBy", form.get("inspectedBy"));
        body.put("observedStatus", form.get("observedStatus"));
        body.put("remarks", form.get("remarks"));
        sanitationClient.addInspection(id, body);
        return "redirect:/sanitation/facilities/" + id;
    }

    @GetMapping("/complaints/new")
    public String newComplaintForm(Model model) {
        try {
            model.addAttribute("facilities", sanitationClient.listFacilities());
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("facilities", List.of());
        }
        return "sanitation/complaint-form";
    }

    @PostMapping("/complaints")
    public String createComplaint(@RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        if (form.get("facilityId") != null && !form.get("facilityId").isBlank()) {
            body.put("facilityId", Long.valueOf(form.get("facilityId")));
        }
        body.put("title", form.get("title"));
        body.put("description", form.get("description"));
        body.put("reportedBy", form.get("reportedBy"));
        body.put("priority", form.get("priority"));
        sanitationClient.createComplaint(body);
        return "redirect:/sanitation";
    }

    @GetMapping("/complaints/{id}")
    public String viewComplaint(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("complaint", sanitationClient.getComplaint(id));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
        }
        return "sanitation/complaint-view";
    }

    @PostMapping("/complaints/{id}/status")
    public String updateComplaintStatus(@PathVariable Long id, @RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", form.get("status"));
        body.put("assignedWorker", form.get("assignedWorker"));
        body.put("closureNotes", form.get("closureNotes"));
        sanitationClient.updateComplaintStatus(id, body);
        return "redirect:/sanitation/complaints/" + id;
    }

    @PostMapping("/complaints/{id}/retry-link")
    public String retryLink(@PathVariable Long id) {
        sanitationClient.retryActionLink(id);
        return "redirect:/sanitation/complaints/" + id;
    }
}
