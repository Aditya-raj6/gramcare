package com.gramcare.action.repository;

import com.gramcare.action.entity.ActionItem;
import com.gramcare.action.entity.DepartmentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartmentAssignmentRepository extends JpaRepository<DepartmentAssignment, Long> {
    Optional<DepartmentAssignment> findByCategory(ActionItem.Category category);
}
