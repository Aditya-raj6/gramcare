package com.gramcare.web.controller;

import com.gramcare.web.client.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Consolidated reports. Each section is loaded independently, so one backend
 * being down only blanks its own section (listed in "unavailable") rather than
 * the whole page.
 */
@Controller
public class ReportsController {

    private final ResidentClient residentClient;
    private final AppointmentCampClient appointmentCampClient;
    private final InventoryClient inventoryClient;
    private final SanitationClient sanitationClient;
    private final ActionClient actionClient;

    public ReportsController(ResidentClient residentClient, AppointmentCampClient appointmentCampClient,
                              InventoryClient inventoryClient, SanitationClient sanitationClient, ActionClient actionClient) {
        this.residentClient = residentClient;
        this.appointmentCampClient = appointmentCampClient;
        this.inventoryClient = inventoryClient;
        this.sanitationClient = sanitationClient;
        this.actionClient = actionClient;
    }

    @GetMapping("/reports")
    public String reports(@RequestParam(required = false) String from,
                           @RequestParam(required = false) String to,
                           @RequestParam(required = false) String category,
                           Model model) {
        LocalDate fromDate = parse(from, LocalDate.now().minusDays(90));
        LocalDate toDate = parse(to, LocalDate.now().plusDays(30));
        List<String> unavailable = new ArrayList<>();

        List<Map<String, Object>> residents = load(() -> residentClient.listResidents(null, null), "Residents", unavailable);
        List<Map<String, Object>> appointments = load(() -> appointmentCampClient.listAppointments(null), "Appointments", unavailable);
        List<Map<String, Object>> camps = load(appointmentCampClient::listHealthCamps, "Health camps", unavailable);
        List<Map<String, Object>> medicines = load(() -> inventoryClient.listMedicines(null), "Inventory", unavailable);
        List<Map<String, Object>> complaints = load(() -> sanitationClient.listComplaints(null), "Sanitation", unavailable);
        List<Map<String, Object>> observations = load(() -> sanitationClient.listObservations(null), "Water quality", unavailable);
        List<Map<String, Object>> actions = load(() -> actionClient.listActions(null, null, null, null), "Actions", unavailable);

        residents = inRange(residents, "registeredAt", fromDate, toDate);
        appointments = inRange(appointments, "appointmentDateTime", fromDate, toDate);
        camps = inRange(camps, "campDate", fromDate, toDate);
        complaints = inRange(complaints, "createdAt", fromDate, toDate);
        observations = inRange(observations, "observationDate", fromDate, toDate);
        actions = inRange(actions, "createdDate", fromDate, toDate);
        if (category != null && !category.isBlank()) {
            actions = actions.stream().filter(a -> category.equals(a.get("category"))).toList();
        }

        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);
        model.addAttribute("category", category);
        model.addAttribute("unavailable", unavailable);

        model.addAttribute("residentsByVillage", countBy(residents, "village"));
        model.addAttribute("residentTotal", residents.size());
        model.addAttribute("appointmentsByStatus", countBy(appointments, "status"));
        model.addAttribute("appointmentTotal", appointments.size());
        model.addAttribute("medicines", medicines);
        model.addAttribute("lowStockCount", medicines.stream().filter(m -> Boolean.TRUE.equals(m.get("lowStock"))).count());
        model.addAttribute("camps", camps);
        model.addAttribute("complaintsByStatus", countBy(complaints, "status"));
        model.addAttribute("complaintTotal", complaints.size());
        model.addAttribute("observationTotal", observations.size());
        model.addAttribute("observationsFlagged", observations.stream().filter(o -> Boolean.TRUE.equals(o.get("outOfRange"))).count());
        model.addAttribute("observationsByVillage", countBy(observations, "village"));
        model.addAttribute("actionsByStatus", countBy(actions, "status"));
        model.addAttribute("actionsByCategory", countBy(actions, "category"));
        model.addAttribute("actionsByVillage", countBy(actions, "village"));
        model.addAttribute("actionTotal", actions.size());
        model.addAttribute("actionsOpen", actions.stream().filter(a -> !"RESOLVED".equals(a.get("status")) && !"CLOSED".equals(a.get("status"))).count());
        model.addAttribute("actionsResolved", actions.stream().filter(a -> "RESOLVED".equals(a.get("status")) || "CLOSED".equals(a.get("status"))).count());
        return "reports/index";
    }

    private List<Map<String, Object>> load(Supplier<List<Map<String, Object>>> s, String name, List<String> unavailable) {
        try {
            return s.get();
        } catch (ServiceUnavailableException ex) {
            unavailable.add(name);
            return List.of();
        }
    }

    private static List<Map<String, Object>> inRange(List<Map<String, Object>> rows, String field, LocalDate from, LocalDate to) {
        return rows.stream().filter(r -> {
            Object v = r.get(field);
            if (v == null) return true; // undated records are still counted
            String s = v.toString();
            if (s.length() < 10) return true;
            try {
                LocalDate d = LocalDate.parse(s.substring(0, 10));
                return !d.isBefore(from) && !d.isAfter(to);
            } catch (Exception e) {
                return true;
            }
        }).toList();
    }

    private static Map<String, Long> countBy(List<Map<String, Object>> rows, String field) {
        return rows.stream().collect(Collectors.groupingBy(
                r -> r.get(field) == null ? "Unspecified" : r.get(field).toString(),
                TreeMap::new, Collectors.counting()));
    }

    private static LocalDate parse(String s, LocalDate fallback) {
        try {
            return (s == null || s.isBlank()) ? fallback : LocalDate.parse(s);
        } catch (Exception e) {
            return fallback;
        }
    }
}
