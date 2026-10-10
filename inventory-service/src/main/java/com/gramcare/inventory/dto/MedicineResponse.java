package com.gramcare.inventory.dto;

import com.gramcare.inventory.entity.Medicine;

public class MedicineResponse {
    private Long id;
    private String name;
    private String genericName;
    private String category;
    private String unitOfMeasure;
    private int minimumStockLevel;
    private int totalQuantity;
    private boolean lowStock;

    public static MedicineResponse from(Medicine m) {
        MedicineResponse dto = new MedicineResponse();
        dto.id = m.getId();
        dto.name = m.getName();
        dto.genericName = m.getGenericName();
        dto.category = m.getCategory();
        dto.unitOfMeasure = m.getUnitOfMeasure();
        dto.minimumStockLevel = m.getMinimumStockLevel();
        dto.totalQuantity = m.getTotalQuantity();
        dto.lowStock = m.isLowStock();
        return dto;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getGenericName() { return genericName; }
    public String getCategory() { return category; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public int getMinimumStockLevel() { return minimumStockLevel; }
    public int getTotalQuantity() { return totalQuantity; }
    public boolean isLowStock() { return lowStock; }
}
