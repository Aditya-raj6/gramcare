package com.healthharbor.healthcamp.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.healthcamp.entity.HealthCamp;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface HealthCampRepository extends CrudRepository<HealthCamp> {
    @Query("select c.status, count(c) from HealthCamp c group by c.status")
    List<Object[]> countByStatus();

    @Query("select c.campType, count(c) from HealthCamp c group by c.campType")
    List<Object[]> countByType();

    List<HealthCamp> findByStatusAndStartDateBetweenOrderByStartDate(HealthCamp.CampStatus status, LocalDate from, LocalDate to);
}
