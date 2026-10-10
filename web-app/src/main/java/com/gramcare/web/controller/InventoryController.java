package com.gramcare.web.controller;

import com.gramcare.web.client.InventoryClient;
import com.gramcare.web.client.LenientMap;
import com.gramcare.web.client.ServiceUnavailableException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryClient inventoryClient;

    public InventoryController(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                        @RequestParam(required = false) Boolean lowStockOnly,
                        Model model) {
        try {
            List<Map<String, Object>> medicines = Boolean.TRUE.equals(lowStockOnly)
                    ? inventoryClient.lowStock() : inventoryClient.listMedicines(name);
            model.addAttribute("medicines", medicines);
            model.addAttribute("recentTransactions", inventoryClient.recentTransactions());
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("medicines", List.of());
            model.addAttribute("recentTransactions", List.of());
        }
        model.addAttribute("nameFilter", name);
        model.addAttribute("lowStockOnly", lowStockOnly);
        return "inventory/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("medicine", new LenientMap());
        return "inventory/form";
    }

    @PostMapping
    public String create(@RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", form.get("name"));
        body.put("genericName", form.get("genericName"));
        body.put("category", form.get("category"));
        body.put("unitOfMeasure", form.get("unitOfMeasure"));
        body.put("minimumStockLevel", Integer.valueOf(form.getOrDefault("minimumStockLevel", "10")));
        inventoryClient.createMedicine(body);
        return "redirect:/inventory";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("medicine", inventoryClient.getMedicine(id));
            model.addAttribute("transactions", inventoryClient.transactionHistory(id));
            model.addAttribute("batches", inventoryClient.batches(id));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("medicine", new LenientMap());
            model.addAttribute("transactions", List.of());
            model.addAttribute("batches", List.of());
        }
        return "inventory/view";
    }

    @PostMapping("/{id}/stock-in")
    public String stockIn(@PathVariable Long id, @RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("batchNumber", form.get("batchNumber"));
        body.put("quantity", Integer.valueOf(form.get("quantity")));
        body.put("expiryDate", blankToNull(form.get("expiryDate")));
        body.put("performedBy", form.get("performedBy"));
        inventoryClient.stockIn(id, body);
        return "redirect:/inventory/" + id;
    }

    @PostMapping("/{id}/issue")
    public String issue(@PathVariable Long id, @RequestParam Map<String, String> form, Model model) {
        Map<String, Object> body = new HashMap<>();
        body.put("quantity", Integer.valueOf(form.get("quantity")));
        body.put("reason", form.get("reason"));
        body.put("performedBy", form.get("performedBy"));
        try {
            inventoryClient.issueStock(id, body);
        } catch (ServiceUnavailableException ex) {
            // Surfaces insufficient-stock / downstream errors back on the detail page.
            return "redirect:/inventory/" + id + "?error=" + java.net.URLEncoder.encode(ex.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/inventory/" + id;
    }

    @GetMapping("/replenishment")
    public String replenishment(@RequestParam(required = false) String status, Model model) {
        try {
            model.addAttribute("requests", inventoryClient.replenishmentRequests(status));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("requests", List.of());
        }
        return "inventory/replenishment";
    }

    @GetMapping("/replenishment/new")
    public String newReplenishmentForm(Model model) {
        try {
            model.addAttribute("medicines", inventoryClient.listMedicines(null));
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("medicines", List.of());
        }
        return "inventory/replenishment-form";
    }

    @PostMapping("/replenishment")
    public String createReplenishment(@RequestParam Map<String, String> form) {
        Map<String, Object> body = new HashMap<>();
        body.put("medicineId", Long.valueOf(form.get("medicineId")));
        body.put("requestedQuantity", Integer.valueOf(form.get("requestedQuantity")));
        body.put("requestedBy", form.get("requestedBy"));
        body.put("notes", form.get("notes"));
        body.put("village", form.get("village"));
        body.put("priority", form.get("priority"));
        inventoryClient.createReplenishmentRequest(body);
        return "redirect:/inventory/replenishment";
    }

    @PostMapping("/replenishment/{id}/retry-link")
    public String retryReplenishmentLink(@PathVariable Long id) {
        inventoryClient.retryReplenishmentLink(id);
        return "redirect:/inventory/replenishment";
    }

    @PostMapping("/replenishment/{id}/status")
    public String updateReplenishmentStatus(@PathVariable Long id, @RequestParam String status) {
        inventoryClient.updateReplenishmentStatus(id, Map.of("status", status));
        return "redirect:/inventory/replenishment";
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
