package com.healthharbor.healthcamp.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.healthcamp.entity.Beneficiary;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BeneficiaryRepository extends CrudRepository<Beneficiary> {
    long countByCampId(Long campId);

    @Query("select b.campName, count(b) from Beneficiary b group by b.campName")
    List<Object[]> countByCamp();
}
