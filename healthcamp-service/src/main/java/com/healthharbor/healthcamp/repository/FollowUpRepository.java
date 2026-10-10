package com.healthharbor.healthcamp.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.healthcamp.entity.FollowUp;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface FollowUpRepository extends CrudRepository<FollowUp> {
    List<FollowUp> findByStatusInOrderByDueDateAsc(Collection<FollowUp.FollowUpStatus> statuses);

    long countByCampId(Long campId);

    long countByCampIdAndStatusIn(Long campId, Collection<FollowUp.FollowUpStatus> statuses);

    @Query("select f.status, count(f) from FollowUp f group by f.status")
    List<Object[]> countByStatus();
}
