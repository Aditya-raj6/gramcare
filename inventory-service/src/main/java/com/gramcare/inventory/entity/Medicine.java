package com.gramcare.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medicines")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    private String genericName;
    private String category;
    private String unitOfMeasure = "units";

    @Column(nullable = false)
    private int minimumStockLevel = 10;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "medicine", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<InventoryBatch> batches = new ArrayList<>();

    @Transient
    @JsonIgnore
    public int getTotalQuantity() {
        // Expired batches are not usable stock, so they do not count towards availability.
        return batches == null ? 0 : batches.stream().filter(b -> !b.isExpired()).mapToInt(InventoryBatch::getQuantity).sum();
    }

    @Transient
    @JsonIgnore
    public boolean isLowStock() {
        return getTotalQuantity() < minimumStockLevel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public int getMinimumStockLevel() { return minimumStockLevel; }
    public void setMinimumStockLevel(int minimumStockLevel) { this.minimumStockLevel = minimumStockLevel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<InventoryBatch> getBatches() { return batches; }
    public void setBatches(List<InventoryBatch> batches) { this.batches = batches; }
}
