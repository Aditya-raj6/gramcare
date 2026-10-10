package com.healthharbor.healthcamp.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.healthcamp.entity.Screening;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScreeningRepository extends CrudRepository<Screening> {
    long countByCampId(Long campId);

    long countByCampIdAndOutcomeNot(Long campId, Screening.Outcome outcome);

    @Query("select s.outcome, count(s) from Screening s group by s.outcome")
    List<Object[]> countByOutcome();

    @Query("select s.screeningType, count(s) from Screening s where s.outcome <> com.healthharbor.healthcamp.entity.Screening.Outcome.NORMAL group by s.screeningType")
    List<Object[]> abnormalByType();
}
