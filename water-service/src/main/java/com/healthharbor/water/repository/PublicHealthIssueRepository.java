package com.healthharbor.water.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.water.entity.PublicHealthIssue;
import com.healthharbor.water.entity.PublicHealthIssue.IssueCategory;
import com.healthharbor.water.entity.PublicHealthIssue.IssueStatus;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface PublicHealthIssueRepository extends CrudRepository<PublicHealthIssue> {
    boolean existsBySourceIdAndCategoryAndStatusIn(Long sourceId, IssueCategory category, Collection<IssueStatus> statuses);

    List<PublicHealthIssue> findByStatusInOrderByIdDesc(Collection<IssueStatus> statuses);

    @Query("select i.category, count(i) from PublicHealthIssue i group by i.category")
    List<Object[]> countByCategory();

    @Query("select i.status, count(i) from PublicHealthIssue i group by i.status")
    List<Object[]> countByStatus();
}
