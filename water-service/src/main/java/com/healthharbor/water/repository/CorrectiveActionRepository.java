package com.healthharbor.water.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.water.entity.CorrectiveAction;

import java.util.Collection;
import java.util.List;

public interface CorrectiveActionRepository extends CrudRepository<CorrectiveAction> {
    List<CorrectiveAction> findByStatusInOrderByPlannedDateAsc(Collection<CorrectiveAction.ActionStatus> statuses);
}
