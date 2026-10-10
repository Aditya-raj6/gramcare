package com.gramcare.action.repository;

import com.gramcare.action.entity.ActionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActionHistoryRepository extends JpaRepository<ActionHistory, Long> {
    List<ActionHistory> findByActionItemIdOrderByChangedAtDesc(Long actionItemId);
}
