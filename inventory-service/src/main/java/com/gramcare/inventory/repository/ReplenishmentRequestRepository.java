
package com.gramcare.inventory.repository;

import com.gramcare.inventory.entity.ReplenishmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Collection;

public interface ReplenishmentRequestRepository
        extends JpaRepository<ReplenishmentRequest, Long> {

    List<ReplenishmentRequest> findByStatus(
            ReplenishmentRequest.RequestStatus status);

    List<ReplenishmentRequest> findByActionItemIdIsNull();

    boolean existsByMedicine_IdAndStatusIn(
            Long medicineId,
            Collection<ReplenishmentRequest.RequestStatus> statuses);
}
