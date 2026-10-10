package com.healthharbor.patient.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.patient.entity.Visit;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface VisitRepository extends CrudRepository<Visit> {

    long countByVisitDateGreaterThanEqual(LocalDate from);

    @Query("select v.visitType, count(v) from Visit v group by v.visitType")
    List<Object[]> countByType();

    @Query("select v.visitDate, count(v) from Visit v where v.visitDate >= :from group by v.visitDate order by v.visitDate")
    List<Object[]> dailyCounts(LocalDate from);
}
