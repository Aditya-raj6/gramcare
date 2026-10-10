package com.gramcare.action.repository;

import com.gramcare.action.entity.ActionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {
    Optional<ActionItem> findBySourceServiceAndSourceReferenceTypeAndSourceReferenceId(
            String sourceService, String sourceReferenceType, Long sourceReferenceId);

    List<ActionItem> findByStatus(ActionItem.Status status);
    List<ActionItem> findByCategory(ActionItem.Category category);
    List<ActionItem> findByVillageIgnoreCase(String village);
    List<ActionItem> findByPriority(ActionItem.Priority priority);
}
