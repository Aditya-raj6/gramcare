package com.healthharbor.water.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.water.entity.WaterQualityTest;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface WaterQualityTestRepository extends CrudRepository<WaterQualityTest> {
    long countByTestDateGreaterThanEqual(LocalDate from);

    @Query("select t.overallStatus, count(t) from WaterQualityTest t group by t.overallStatus")
    List<Object[]> countByStatus();

    @Query("select t.village, count(t) from WaterQualityTest t where t.overallStatus = com.healthharbor.water.entity.WaterSource.QualityStatus.UNSAFE group by t.village")
    List<Object[]> unsafeByVillage();
}
