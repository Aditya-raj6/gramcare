package com.gramcare.action;

import com.gramcare.action.dto.ActionItemRequest;
import com.gramcare.action.dto.StatusUpdateRequest;
import com.gramcare.action.entity.ActionItem;
import com.gramcare.action.service.ActionItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ActionItemServiceTests {

    @Autowired
    private ActionItemService actionItemService;

    private ActionItemRequest buildRequest(long refId) {
        ActionItemRequest req = new ActionItemRequest();
        req.setTitle("Toilet needs repair");
        req.setCategory(ActionItem.Category.SANITATION);
        req.setVillage("Kondapur");
        req.setPriority(ActionItem.Priority.HIGH);
        req.setSourceService("sanitation-service");
        req.setSourceReferenceType("SANITATION_COMPLAINT");
        req.setSourceReferenceId(refId);
        return req;
    }

    @Test
    void retriedCreationDoesNotDuplicateActionItem() {
        long refId = System.nanoTime();
        ActionItem first = actionItemService.createOrGetExisting(buildRequest(refId));
        ActionItem second = actionItemService.createOrGetExisting(buildRequest(refId));

        assertEquals(first.getId(), second.getId());
    }

    @Test
    void statusUpdateRecordsHistory() {
        long refId = System.nanoTime();
        ActionItem item = actionItemService.createOrGetExisting(buildRequest(refId));

        StatusUpdateRequest statusReq = new StatusUpdateRequest();
        statusReq.setStatus(ActionItem.Status.RESOLVED);
        statusReq.setNotes("Repaired the broken latch");
        statusReq.setChangedBy("Sanitation Officer");
        actionItemService.updateStatus(item.getId(), statusReq);

        // one entry for creation, one for the status change
        assertEquals(2, actionItemService.getHistory(item.getId()).size());
        assertEquals(ActionItem.Status.RESOLVED, actionItemService.findById(item.getId()).getStatus());
    }

    @Test
    void assigningAPersonMovesOpenItemToAssigned() {
        ActionItem item = actionItemService.createOrGetExisting(buildRequest(System.nanoTime()));
        com.gramcare.action.dto.AssignRequest assign = new com.gramcare.action.dto.AssignRequest();
        assign.setAssignedDepartment("Sanitation Department");
        assign.setAssignedPerson("R. Patil");
        assign.setChangedBy("Administrator");
        ActionItem updated = actionItemService.assign(item.getId(), assign);

        assertEquals(ActionItem.Status.ASSIGNED, updated.getStatus());
        assertEquals("R. Patil", updated.getAssignedPerson());
    }

    @Test
    void highPriorityItemGetsDefaultDueDateAndIsNotOverdue() {
        ActionItem item = actionItemService.createOrGetExisting(buildRequest(System.nanoTime()));
        assertEquals(java.time.LocalDate.now().plusDays(3), item.getDueDate());
        assertFalse(item.isOverdue());
    }
}
