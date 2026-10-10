package com.healthharbor.inventory.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.inventory.entity.StockItem;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockItemRepository extends CrudRepository<StockItem> {

    @Query("select s from StockItem s where s.quantity <= s.reorderLevel or s.expiryDate < CURRENT_DATE order by s.quantity")
    List<StockItem> findAttentionRequired();

    List<StockItem> findByMedicineIdAndHealthCentreIdOrderByExpiryDateAsc(Long medicineId, Long healthCentreId);

    @Query("select s.healthCentreName, sum(s.quantity) from StockItem s group by s.healthCentreName")
    List<Object[]> quantityByCentre();

    @Query("select s.category, sum(s.quantity) from StockItem s group by s.category")
    List<Object[]> quantityByCategory();

    @Query("select coalesce(sum(s.quantity * coalesce(s.unitPrice, 0)), 0) from StockItem s")
    Double totalValue();
}
