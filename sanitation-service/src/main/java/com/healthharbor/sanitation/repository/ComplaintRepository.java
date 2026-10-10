package com.healthharbor.sanitation.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.sanitation.entity.Complaint;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ComplaintRepository extends CrudRepository<Complaint> {
    List<Complaint> findByMaintenanceTaskId(Long taskId);

    List<Complaint> findByStatusInOrderByIdDesc(Collection<Complaint.ComplaintStatus> statuses);

    @Query("select c.category, count(c) from Complaint c group by c.category")
    List<Object[]> countByCategory();

    @Query("select c.status, count(c) from Complaint c group by c.status")
    List<Object[]> countByStatus();

    @Query("select c.village, count(c) from Complaint c group by c.village")
    List<Object[]> countByVillage();
}
