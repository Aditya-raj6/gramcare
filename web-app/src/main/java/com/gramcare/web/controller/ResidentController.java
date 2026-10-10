package com.gramcare.web.controller;

import com.gramcare.web.client.ResidentClient;
import com.gramcare.web.client.LenientMap;
import com.gramcare.web.client.ServiceUnavailableException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/residents")
public class ResidentController {

    private final ResidentClient residentClient;

    public ResidentController(ResidentClient residentClient) {
        this.residentClient = residentClient;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                        @RequestParam(required = false) String village,
                        Model model) {
        try {
            model.addAttribute("residents", residentClient.listResidents(name, village));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("residents", List.of());
        }
        model.addAttribute("nameFilter", name);
        model.addAttribute("villageFilter", village);
        return "residents/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("resident", new LenientMap());
        return "residents/form";
    }

    @PostMapping
    public String create(@RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("fullName", form.get("fullName"));
        body.put("dateOfBirth", blankToNull(form.get("dateOfBirth")));
        body.put("gender", blankToNull(form.get("gender")));
        body.put("village", form.get("village"));
        body.put("householdAddress", form.get("householdAddress"));
        body.put("contactPhone", form.get("contactPhone"));
        body.put("bloodGroup", form.get("bloodGroup"));
        body.put("knownAllergies", form.get("knownAllergies"));
        body.put("chronicConditions", form.get("chronicConditions"));
        residentClient.createResident(body);
        return "redirect:/residents";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("resident", residentClient.getResident(id));
            model.addAttribute("visits", residentClient.getVisits(id));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("resident", new LenientMap());
            model.addAttribute("visits", List.of());
        }
        return "residents/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("resident", residentClient.getResident(id));
        return "residents/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("fullName", form.get("fullName"));
        body.put("dateOfBirth", blankToNull(form.get("dateOfBirth")));
        body.put("gender", blankToNull(form.get("gender")));
        body.put("village", form.get("village"));
        body.put("householdAddress", form.get("householdAddress"));
        body.put("contactPhone", form.get("contactPhone"));
        body.put("bloodGroup", form.get("bloodGroup"));
        body.put("knownAllergies", form.get("knownAllergies"));
        body.put("chronicConditions", form.get("chronicConditions"));
        residentClient.updateResident(id, body);
        return "redirect:/residents/" + id;
    }

    @PostMapping("/{id}/visits")
    public String addVisit(@PathVariable Long id, @RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("visitDate", blankToNull(form.get("visitDate")));
        body.put("reasonForVisit", form.get("reasonForVisit"));
        body.put("diagnosisSummary", form.get("diagnosisSummary"));
        body.put("notes", form.get("notes"));
        residentClient.addVisit(id, body);
        return "redirect:/residents/" + id;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
