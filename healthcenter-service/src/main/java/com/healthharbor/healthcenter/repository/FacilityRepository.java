package com.healthharbor.healthcenter.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.healthcenter.entity.Facility;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FacilityRepository extends CrudRepository<Facility> {
    @Query("select f.status, count(f) from Facility f group by f.status")
    List<Object[]> countByStatus();

    List<Facility> findByStatusNot(Facility.FacilityStatus status);
}
