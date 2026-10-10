package com.healthharbor.healthcamp.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.healthcamp.entity.CampStaff;

public interface CampStaffRepository extends CrudRepository<CampStaff> {
    long countByCampId(Long campId);
}
