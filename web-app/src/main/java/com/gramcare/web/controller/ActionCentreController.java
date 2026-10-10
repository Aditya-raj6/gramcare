package com.gramcare.web.controller;

import com.gramcare.web.client.ActionClient;
import com.gramcare.web.client.ServiceUnavailableException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/actions")
public class ActionCentreController {

    private final ActionClient actionClient;

    public ActionCentreController(ActionClient actionClient) {
        this.actionClient = actionClient;
    }

    /**
     * Fetches all action items once and applies every filter here, so filters
     * can be combined (e.g. SANITATION + HIGH + Kondapur + overdue).
     */
    @GetMapping
    public String list(@RequestParam(required = false) String q,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) String village,
                        @RequestParam(required = false) String priority,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String assignee,
                        @RequestParam(required = false) Boolean overdueOnly,
                        Model model) {
        List<Map<String, Object>> all;
        try {
            all = actionClient.listActions(null, null, null, null);
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            all = List.of();
        }

        List<Map<String, Object>> filtered = all.stream()
                .filter(a -> isBlank(q) || contains(a.get("title"), q) || contains(a.get("description"), q) || String.valueOf(a.get("id")).equals(q.trim()))
                .filter(a -> isBlank(category) || category.equals(a.get("category")))
                .filter(a -> isBlank(village) || contains(a.get("village"), village))
                .filter(a -> isBlank(priority) || priority.equals(a.get("priority")))
                .filter(a -> isBlank(status) || status.equals(a.get("status")))
                .filter(a -> isBlank(assignee) || contains(a.get("assignedPerson"), assignee) || contains(a.get("assignedDepartment"), assignee))
                .filter(a -> !Boolean.TRUE.equals(overdueOnly) || Boolean.TRUE.equals(a.get("overdue")))
                .sorted(Comparator.comparing((Map<String, Object> a) -> priorityRank(a.get("priority")))
                        .thenComparing(a -> String.valueOf(a.get("createdDate")), Comparator.reverseOrder()))
                .collect(Collectors.toList());

        model.addAttribute("actions", filtered);
        model.addAttribute("totalCount", all.size());
        model.addAttribute("villages", all.stream().map(a -> a.get("village")).filter(Objects::nonNull).map(Object::toString).distinct().sorted().toList());
        model.addAttribute("q", q);
        model.addAttribute("category", category);
        model.addAttribute("village", village);
        model.addAttribute("priority", priority);
        model.addAttribute("status", status);
        model.addAttribute("assignee", assignee);
        model.addAttribute("overdueOnly", overdueOnly);
        return "actions/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        // A per-form token doubles as the idempotency key: submitting the same
        // form twice (double click, browser retry) cannot create two items.
        model.addAttribute("formToken", Math.abs(UUID.randomUUID().getMostSignificantBits()));
        return "actions/form";
    }

    @PostMapping
    public String create(@RequestParam Map<String, String> form, HttpSession session) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", form.get("title"));
        body.put("description", form.get("description"));
        body.put("category", form.get("category"));
        body.put("village", form.get("village"));
        body.put("priority", form.get("priority"));
        body.put("dueDate", isBlank(form.get("dueDate")) ? null : form.get("dueDate"));
        body.put("reportedBy", isBlank(form.get("reportedBy")) ? String.valueOf(session.getAttribute("demoRole")) : form.get("reportedBy"));
        body.put("sourceService", "web-app");
        body.put("sourceReferenceType", "MANUAL_REPORT");
        body.put("sourceReferenceId", Long.valueOf(form.get("formToken")));
        Map<String, Object> created = actionClient.create(body);
        return "redirect:/actions/" + created.get("id");
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("action", actionClient.getAction(id));
            model.addAttribute("history", actionClient.getHistory(id));
            model.addAttribute("departments", actionClient.departments());
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", ex.getMessage());
            model.addAttribute("history", List.of());
            model.addAttribute("departments", List.of());
        }
        return "actions/view";
    }

    @PostMapping("/{id}/assign")
    public String assign(@PathVariable Long id, @RequestParam Map<String, String> form, HttpSession session) {
        Map<String, Object> body = new HashMap<>();
        body.put("assignedDepartment", form.get("assignedDepartment"));
        body.put("assignedPerson", form.get("assignedPerson"));
        body.put("changedBy", String.valueOf(session.getAttribute("demoRole")));
        actionClient.assign(id, body);
        return "redirect:/actions/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam Map<String, String> form, HttpSession session) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", form.get("status"));
        body.put("notes", form.get("notes"));
        body.put("changedBy", String.valueOf(session.getAttribute("demoRole")));
        actionClient.updateStatus(id, body);
        return "redirect:/actions/" + id;
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    private static boolean contains(Object haystack, String needle) {
        return haystack != null && haystack.toString().toLowerCase().contains(needle.trim().toLowerCase());
    }

    private static int priorityRank(Object p) {
        if (p == null) return 9;
        return switch (p.toString()) { case "CRITICAL" -> 0; case "HIGH" -> 1; case "MEDIUM" -> 2; case "LOW" -> 3; default -> 9; };
    }
}
