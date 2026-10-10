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
@RequestMapping("/centres")
public class CentreController {

    private final ResidentClient residentClient;

    public CentreController(ResidentClient residentClient) {
        this.residentClient = residentClient;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                        @RequestParam(required = false) String village,
                        Model model) {
        try {
            model.addAttribute("centres", residentClient.listHealthCentres(name, village));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("centres", List.of());
        }
        model.addAttribute("nameFilter", name);
        model.addAttribute("villageFilter", village);
        return "centres/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("centre", new LenientMap());
        return "centres/form";
    }

    @PostMapping
    public String create(@RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", form.get("name"));
        body.put("village", form.get("village"));
        body.put("address", form.get("address"));
        body.put("contactPhone", form.get("contactPhone"));
        body.put("contactEmail", form.get("contactEmail"));
        body.put("centreType", blankToNull(form.get("centreType")));
        residentClient.createHealthCentre(body);
        return "redirect:/centres";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("centre", residentClient.getHealthCentre(id));
            model.addAttribute("workers", residentClient.getWorkers(id));
            model.addAttribute("services", residentClient.getServices(id));
            model.addAttribute("schedules", residentClient.getSchedules(id));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("centre", new LenientMap());
            model.addAttribute("workers", List.of());
            model.addAttribute("services", List.of());
            model.addAttribute("schedules", List.of());
        }
        return "centres/view";
    }

    @PostMapping("/{id}/workers")
    public String addWorker(@PathVariable Long id, @RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("fullName", form.get("fullName"));
        body.put("role", blankToNull(form.get("role")));
        body.put("qualification", form.get("qualification"));
        body.put("contactPhone", form.get("contactPhone"));
        residentClient.addWorker(id, body);
        return "redirect:/centres/" + id;
    }

    @PostMapping("/{id}/services")
    public String addService(@PathVariable Long id, @RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("serviceName", form.get("serviceName"));
        body.put("description", form.get("description"));
        residentClient.addService(id, body);
        return "redirect:/centres/" + id;
    }

    @PostMapping("/{id}/schedules")
    public String addSchedule(@PathVariable Long id, @RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("dayOfWeek", blankToNull(form.get("dayOfWeek")));
        body.put("openTime", blankToNull(form.get("openTime")));
        body.put("closeTime", blankToNull(form.get("closeTime")));
        body.put("closed", "on".equals(form.get("closed")));
        residentClient.addSchedule(id, body);
        return "redirect:/centres/" + id;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
