package com.healthharbor.common.crud;

import com.healthharbor.common.dto.StatusUpdateRequest;
import com.healthharbor.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Standard REST surface: GET list (with field filters), GET one, POST, PUT, PATCH status, DELETE.
 */
public abstract class CrudController<T extends BaseEntity> {

    protected final CrudService<T> service;

    protected CrudController(CrudService<T> service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List records; any entity field can be passed as an equality filter")
    public List<T> list(@RequestParam Map<String, String> params) {
        return service.list(params);
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Get a record by id")
    public T get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a record")
    public T create(@Valid @RequestBody T entity) {
        return service.create(entity);
    }

    @PutMapping("/{id:\\d+}")
    @Operation(summary = "Update a record (null fields are left unchanged)")
    public T update(@PathVariable Long id, @Valid @RequestBody T entity) {
        return service.update(id, entity);
    }

    @PatchMapping("/{id:\\d+}/status")
    @Operation(summary = "Move a record along its workflow")
    public T updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return service.updateStatus(id, request);
    }

    @DeleteMapping("/{id:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a record")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
