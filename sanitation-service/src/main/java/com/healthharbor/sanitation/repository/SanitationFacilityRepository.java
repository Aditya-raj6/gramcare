package com.healthharbor.sanitation.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.sanitation.entity.SanitationFacility;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SanitationFacilityRepository extends CrudRepository<SanitationFacility> {
    @Query("select f.cleanlinessStatus, count(f) from SanitationFacility f group by f.cleanlinessStatus")
    List<Object[]> countByCleanliness();

    @Query("select f.status, count(f) from SanitationFacility f group by f.status")
    List<Object[]> countByStatus();

    @Query("select f.type, count(f) from SanitationFacility f group by f.type")
    List<Object[]> countByType();
}
