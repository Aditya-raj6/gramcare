package com.healthharbor.sanitation.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.sanitation.entity.MaintenanceTask;
import com.healthharbor.sanitation.entity.MaintenanceTask.MaintenanceType;
import com.healthharbor.sanitation.entity.MaintenanceTask.TaskStatus;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface MaintenanceTaskRepository extends CrudRepository<MaintenanceTask> {
    boolean existsByFacilityIdAndStatusAndIdNot(Long facilityId, TaskStatus status, Long id);

    boolean existsByFacilityIdAndMaintenanceTypeAndStatusIn(Long facilityId, MaintenanceType type, Collection<TaskStatus> statuses);

    List<MaintenanceTask> findByStatusInOrderByIdDesc(Collection<TaskStatus> statuses);

    @Query("select t.status, count(t) from MaintenanceTask t group by t.status")
    List<Object[]> countByStatus();

    @Query("select t.maintenanceType, count(t) from MaintenanceTask t group by t.maintenanceType")
    List<Object[]> countByType();
}
