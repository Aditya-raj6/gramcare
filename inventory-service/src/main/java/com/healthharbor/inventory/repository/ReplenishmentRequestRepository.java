package com.healthharbor.inventory.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.inventory.entity.ReplenishmentRequest;
import com.healthharbor.inventory.entity.ReplenishmentRequest.RequestStatus;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ReplenishmentRequestRepository extends CrudRepository<ReplenishmentRequest> {

    boolean existsByMedicineIdAndHealthCentreIdAndStatusIn(Long medicineId, Long centreId, Collection<RequestStatus> statuses);

    List<ReplenishmentRequest> findByStatusInOrderByIdDesc(Collection<RequestStatus> statuses);

    long countByStatusIn(Collection<RequestStatus> statuses);

    @Query("select r.status, count(r) from ReplenishmentRequest r group by r.status")
    List<Object[]> countByStatus();
}
