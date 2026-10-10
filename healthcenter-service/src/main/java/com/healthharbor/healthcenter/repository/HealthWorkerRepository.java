package com.healthharbor.healthcenter.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.healthcenter.entity.HealthWorker;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HealthWorkerRepository extends CrudRepository<HealthWorker> {
    @Query("select w.role, count(w) from HealthWorker w group by w.role")
    List<Object[]> countByRole();

    long countByStatus(HealthWorker.WorkerStatus status);
}
