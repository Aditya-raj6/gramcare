package com.healthharbor.inventory.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.inventory.entity.Medicine;
import com.healthharbor.inventory.repository.MedicineRepository;
import org.springframework.stereotype.Service;

@Service
public class MedicineService extends CrudService<Medicine> {

    public MedicineService(MedicineRepository repository) {
        super(repository, Medicine.class, "Medicine");
    }

    @Override
    protected void afterCreate(Medicine entity) {
        if (entity.getCode() == null || entity.getCode().isBlank()) {
            entity.setCode(reference("MED", entity.getId()));
            repository.save(entity);
        }
    }
}
