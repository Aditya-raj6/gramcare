package com.healthharbor.water.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.water.entity.WaterSource;
import com.healthharbor.water.repository.WaterSourceRepository;
import org.springframework.stereotype.Service;

@Service
public class WaterSourceService extends CrudService<WaterSource> {

    public WaterSourceService(WaterSourceRepository repository) {
        super(repository, WaterSource.class, "Water source");
    }

    public WaterSource require(Long id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException("Water source " + id + " does not exist"));
    }

    void save(WaterSource source) {
        repository.save(source);
    }

    @Override
    protected void afterCreate(WaterSource entity) {
        if (entity.getSourceCode() == null || entity.getSourceCode().isBlank()) {
            entity.setSourceCode(reference("WS", entity.getId()));
            repository.save(entity);
        }
    }
}
