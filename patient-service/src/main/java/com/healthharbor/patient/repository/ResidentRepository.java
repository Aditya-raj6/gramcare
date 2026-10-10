package com.healthharbor.patient.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.patient.entity.Resident;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResidentRepository extends CrudRepository<Resident> {

    @Query("select r.village, count(r) from Resident r group by r.village order by count(r) desc")
    List<Object[]> countByVillage();

    @Query("select r.gender, count(r) from Resident r group by r.gender")
    List<Object[]> countByGender();

    long countByChronicConditionsIsNotNullAndChronicConditionsNot(String empty);

    long countByPregnantTrue();
}
