package com.gramcare.web.controller;

import com.gramcare.web.client.AppointmentCampClient;
import com.gramcare.web.client.InventoryClient;
import com.gramcare.web.client.LenientMap;
import com.gramcare.web.client.ResidentClient;
import com.gramcare.web.client.LenientMap;
import com.gramcare.web.client.ServiceUnavailableException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentCampClient appointmentClient;
    private final ResidentClient residentClient;
    private final InventoryClient inventoryClient;

    public AppointmentController(AppointmentCampClient appointmentClient, ResidentClient residentClient,
                                  InventoryClient inventoryClient) {
        this.appointmentClient = appointmentClient;
        this.residentClient = residentClient;
        this.inventoryClient = inventoryClient;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String view, Model model) {
        try {
            List<Map<String, Object>> appointments;
            if ("today".equals(view)) {
                appointments = appointmentClient.todaysAppointments();
            } else if ("upcoming".equals(view)) {
                appointments = appointmentClient.upcomingAppointments();
            } else {
                appointments = appointmentClient.listAppointments(null);
            }
            model.addAttribute("appointments", appointments);
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("appointments", List.of());
        }
        model.addAttribute("view", view);
        return "appointments/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        try {
            model.addAttribute("residents", residentClient.listResidents(null, null));
            model.addAttribute("centres", residentClient.listHealthCentres(null, null));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("residents", List.of());
            model.addAttribute("centres", List.of());
        }
        return "appointments/form";
    }

    @PostMapping
    public String create(@RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("residentId", Long.valueOf(form.get("residentId")));
        if (form.get("healthCentreId") != null && !form.get("healthCentreId").isBlank()) {
            body.put("healthCentreId", Long.valueOf(form.get("healthCentreId")));
        }
        body.put("appointmentDateTime", form.get("appointmentDateTime"));
        body.put("reason", form.get("reason"));
        appointmentClient.createAppointment(body);
        return "redirect:/appointments";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("appointment", appointmentClient.getAppointment(id));
            List<Map<String, Object>> consultations = appointmentClient.getConsultations(id);
            Map<Object, List<Map<String, Object>>> treatments = new HashMap<>();
            for (Map<String, Object> c : consultations) {
                treatments.put(c.get("id"), appointmentClient.getTreatments(((Number) c.get("id")).longValue()));
            }
            model.addAttribute("consultations", consultations);
            model.addAttribute("treatments", treatments);
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("appointment", new LenientMap());
            model.addAttribute("consultations", List.of());
            model.addAttribute("treatments", Map.of());
        }
        // Inventory is optional on this page: if it is down, treatments can
        // still be recorded, just without dispensing from stock.
        try {
            model.addAttribute("medicines", inventoryClient.listMedicines(null));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("medicines", List.of());
            model.addAttribute("inventoryUnavailable", true);
        }
        return "appointments/view";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        appointmentClient.updateAppointmentStatus(id, Map.of("status", status));
        return "redirect:/appointments/" + id;
    }

    @PostMapping("/{id}/consultations")
    public String addConsultation(@PathVariable Long id, @RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("symptoms", form.get("symptoms"));
        body.put("diagnosis", form.get("diagnosis"));
        body.put("notes", form.get("notes"));
        appointmentClient.addConsultation(id, body);
        return "redirect:/appointments/" + id;
    }

    /**
     * Records a treatment and, if a medicine and quantity were chosen, issues
     * that stock from inventory-service first. Issuing first means an
     * insufficient-stock rejection stops the whole step with a clear message,
     * instead of saving a prescription that was never actually dispensed.
     * Issuing can push the medicine below its minimum, which automatically
     * raises a replenishment request and a MEDICINE_STOCK action item.
     */
    @PostMapping("/consultations/{consultationId}/treatments")
    public String addTreatment(@PathVariable Long consultationId, @RequestParam Long appointmentId,
                                @RequestParam Map<String, String> form, HttpSession session) {
        String medicineId = form.get("dispenseMedicineId");
        String quantity = form.get("dispenseQuantity");
        String dispensedNote = "";
        if (medicineId != null && !medicineId.isBlank() && quantity != null && !quantity.isBlank()) {
            Map<String, Object> issue = new HashMap<>();
            issue.put("quantity", Integer.valueOf(quantity));
            issue.put("reason", "Dispensed for appointment #" + appointmentId);
            issue.put("performedBy", String.valueOf(session.getAttribute("demoRole")));
            inventoryClient.issueStock(Long.valueOf(medicineId), issue);
            dispensedNote = " [Dispensed " + quantity + " from stock]";
        }
        Map<String, Object> body = new HashMap<>();
        body.put("medicationPrescribed", form.get("medicationPrescribed"));
        body.put("dosageInstructions", form.get("dosageInstructions"));
        body.put("followUpDate", form.get("followUpDate") == null || form.get("followUpDate").isBlank() ? null : form.get("followUpDate"));
        body.put("notes", (form.get("notes") == null ? "" : form.get("notes")) + dispensedNote);
        appointmentClient.addTreatment(consultationId, body);
        return "redirect:/appointments/" + appointmentId;
    }
}
