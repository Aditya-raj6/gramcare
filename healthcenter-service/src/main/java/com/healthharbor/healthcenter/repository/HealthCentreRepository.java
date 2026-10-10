package com.healthharbor.healthcenter.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.healthcenter.entity.HealthCentre;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HealthCentreRepository extends CrudRepository<HealthCentre> {
    @Query("select c.type, count(c) from HealthCentre c group by c.type")
    List<Object[]> countByType();

    @Query("select c.status, count(c) from HealthCentre c group by c.status")
    List<Object[]> countByStatus();

    List<HealthCentre> findByStatusNot(HealthCentre.CentreStatus status);
}
