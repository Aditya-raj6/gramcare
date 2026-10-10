package com.healthharbor.inventory.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.inventory.entity.StockTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface StockTransactionRepository extends CrudRepository<StockTransaction> {

    @Query("select t.medicineName, sum(-t.quantity) from StockTransaction t where t.type = :type and t.transactionDate >= :from group by t.medicineName order by sum(-t.quantity) desc")
    List<Object[]> topIssued(StockTransaction.TransactionType type, LocalDateTime from, Pageable pageable);

    long countByTypeAndTransactionDateGreaterThanEqual(StockTransaction.TransactionType type, LocalDateTime from);
}
