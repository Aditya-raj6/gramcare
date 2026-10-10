package com.gramcare.web.controller;

import com.gramcare.web.client.AppointmentCampClient;
import com.gramcare.web.client.LenientMap;
import com.gramcare.web.client.ServiceUnavailableException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/camps")
public class CampController {

    private final AppointmentCampClient campClient;

    public CampController(AppointmentCampClient campClient) {
        this.campClient = campClient;
    }

    @GetMapping
    public String list(Model model) {
        try {
            model.addAttribute("camps", campClient.listHealthCamps());
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("camps", List.of());
        }
        return "camps/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        return "camps/form";
    }

    @PostMapping
    public String create(@RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("campName", form.get("campName"));
        body.put("village", form.get("village"));
        body.put("campDate", form.get("campDate"));
        body.put("location", form.get("location"));
        campClient.createHealthCamp(body);
        return "redirect:/camps";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("camp", campClient.getHealthCamp(id));
            model.addAttribute("staff", campClient.getStaff(id));
            model.addAttribute("beneficiaries", campClient.getBeneficiaries(id));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("camp", new LenientMap());
            model.addAttribute("staff", List.of());
            model.addAttribute("beneficiaries", List.of());
        }
        return "camps/view";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        campClient.updateCampStatus(id, Map.of("status", status));
        return "redirect:/camps/" + id;
    }

    @PostMapping("/{id}/staff")
    public String assignStaff(@PathVariable Long id, @RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("workerName", form.get("workerName"));
        body.put("role", form.get("role"));
        campClient.assignStaff(id, body);
        return "redirect:/camps/" + id;
    }

    @PostMapping("/{id}/beneficiaries")
    public String registerBeneficiary(@PathVariable Long id, @RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("beneficiaryName", form.get("beneficiaryName"));
        body.put("age", form.get("age") != null && !form.get("age").isBlank() ? Integer.valueOf(form.get("age")) : null);
        body.put("contactPhone", form.get("contactPhone"));
        campClient.registerBeneficiary(id, body);
        return "redirect:/camps/" + id;
    }

    @GetMapping("/beneficiaries/{beneficiaryId}")
    public String beneficiary(@PathVariable Long beneficiaryId, @RequestParam Long campId, Model model) {
        try {
            model.addAttribute("screenings", campClient.getScreenings(beneficiaryId));
            model.addAttribute("followUps", campClient.getFollowUps(beneficiaryId));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("screenings", List.of());
            model.addAttribute("followUps", List.of());
        }
        model.addAttribute("beneficiaryId", beneficiaryId);
        model.addAttribute("campId", campId);
        return "camps/beneficiary";
    }

    @PostMapping("/beneficiaries/{beneficiaryId}/screenings")
    public String addScreening(@PathVariable Long beneficiaryId, @RequestParam Long campId, @RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("screeningType", form.get("screeningType"));
        body.put("result", form.get("result"));
        body.put("remarks", form.get("remarks"));
        body.put("referralNeeded", "on".equals(form.get("referralNeeded")));
        campClient.addScreening(beneficiaryId, body);
        return "redirect:/camps/beneficiaries/" + beneficiaryId + "?campId=" + campId;
    }

    @PostMapping("/beneficiaries/{beneficiaryId}/follow-ups")
    public String addFollowUp(@PathVariable Long beneficiaryId, @RequestParam Long campId, @RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("scheduledDate", form.get("scheduledDate"));
        body.put("notes", form.get("notes"));
        campClient.addFollowUp(beneficiaryId, body);
        return "redirect:/camps/beneficiaries/" + beneficiaryId + "?campId=" + campId;
    }
}
