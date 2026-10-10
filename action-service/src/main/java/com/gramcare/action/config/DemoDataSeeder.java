package com.gramcare.action.config;

import com.gramcare.action.dto.ActionItemRequest;
import com.gramcare.action.entity.ActionItem;
import com.gramcare.action.entity.DepartmentAssignment;
import com.gramcare.action.repository.ActionItemRepository;
import com.gramcare.action.repository.DepartmentAssignmentRepository;
import com.gramcare.action.service.ActionItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Seeds routing rules and a few manually reported demo issues. Runs only when
 * the tables are empty, so restarting the service never duplicates data.
 * Sanitation, water and medicine action items are NOT seeded here: those
 * services create them through the REST API (and reconcile automatically).
 */
@Component
@Profile("!test")
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final DepartmentAssignmentRepository departmentRepository;
    private final ActionItemRepository actionItemRepository;
    private final ActionItemService actionItemService;

    public DemoDataSeeder(DepartmentAssignmentRepository departmentRepository,
                           ActionItemRepository actionItemRepository,
                           ActionItemService actionItemService) {
        this.departmentRepository = departmentRepository;
        this.actionItemRepository = actionItemRepository;
        this.actionItemService = actionItemService;
    }

    @Override
    public void run(String... args) {
        if (departmentRepository.count() == 0) {
            dept(ActionItem.Category.SANITATION, "Sanitation Department", "R. Patil");
            dept(ActionItem.Category.MEDICINE_STOCK, "Pharmacy & Inventory", "S. Kulkarni");
            dept(ActionItem.Category.WATER_QUALITY, "Water Supply & Public Health", "A. Deshmukh");
            dept(ActionItem.Category.HEALTH_CAMP, "Health Camp Coordination", "Dr. M. Iyer");
            dept(ActionItem.Category.GENERAL_HEALTH, "Primary Health Centre", "Dr. N. Rao");
            log.info("Seeded department routing rules");
        }
        if (actionItemRepository.count() == 0) {
            manual(1L, "Follow up on 6 residents referred at Kondapur eye camp", ActionItem.Category.HEALTH_CAMP,
                    "Kondapur", ActionItem.Priority.MEDIUM, LocalDate.now().plusDays(5));
            manual(2L, "Fever cluster reported in ward 3, Shivnagar", ActionItem.Category.GENERAL_HEALTH,
                    "Shivnagar", ActionItem.Priority.HIGH, LocalDate.now().minusDays(1));
            manual(3L, "Arrange ambulance tie-up for Rampur sub centre", ActionItem.Category.GENERAL_HEALTH,
                    "Rampur", ActionItem.Priority.LOW, LocalDate.now().plusDays(20));
            log.info("Seeded demo action items");
        }
    }

    private void dept(ActionItem.Category category, String name, String defaultAssignee) {
        DepartmentAssignment d = new DepartmentAssignment();
        d.setCategory(category);
        d.setDepartmentName(name);
        d.setDefaultAssignee(defaultAssignee);
        departmentRepository.save(d);
    }

    private void manual(Long ref, String title, ActionItem.Category category, String village,
                         ActionItem.Priority priority, LocalDate due) {
        ActionItemRequest req = new ActionItemRequest();
        req.setTitle(title);
        req.setDescription("Demo item seeded for the hackathon walkthrough.");
        req.setCategory(category);
        req.setVillage(village);
        req.setPriority(priority);
        req.setReportedBy("ASHA worker (demo)");
        req.setSourceService("demo-seed");
        req.setSourceReferenceType("MANUAL_REPORT");
        req.setSourceReferenceId(ref);
        req.setDueDate(due);
        actionItemService.createOrGetExisting(req);
    }
}
