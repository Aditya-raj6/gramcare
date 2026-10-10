package com.healthharbor.water.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.water.entity.WaterSource;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WaterSourceRepository extends CrudRepository<WaterSource> {
    @Query("select s.status, count(s) from WaterSource s group by s.status")
    List<Object[]> countByStatus();

    @Query("select s.lastQualityStatus, count(s) from WaterSource s group by s.lastQualityStatus")
    List<Object[]> countByQuality();

    @Query("select s.type, count(s) from WaterSource s group by s.type")
    List<Object[]> countByType();

    @Query("select coalesce(sum(s.populationServed), 0) from WaterSource s")
    Long totalPopulationServed();
}
