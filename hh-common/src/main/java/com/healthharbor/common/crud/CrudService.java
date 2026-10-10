package com.healthharbor.common.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthharbor.common.dto.StatusUpdateRequest;
import com.healthharbor.common.entity.BaseEntity;
import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.common.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Generic service providing list/filter/get/create/update/delete/status-transition for an entity.
 * Concrete services customise behaviour through the {@code before*}/{@code after*} hooks.
 * <p>
 * Any request parameter whose name matches an entity field is applied as an equality filter,
 * e.g. {@code GET /api/stock?healthCentreId=2}.
 */
public abstract class CrudService<T extends BaseEntity> {

    private static final Set<String> RESERVED_PARAMS = Set.of("sort", "q", "page", "size");
    private static final ObjectMapper CONVERTER = new ObjectMapper().registerModule(new JavaTimeModule());

    protected final CrudRepository<T> repository;
    private final Class<T> type;
    private final String resourceName;

    protected CrudService(CrudRepository<T> repository, Class<T> type, String resourceName) {
        this.repository = repository;
        this.type = type;
        this.resourceName = resourceName;
    }

    @Transactional(readOnly = true)
    public List<T> list(Map<String, String> params) {
        Specification<T> spec = buildSpecification(params == null ? Map.of() : params);
        return repository.findAll(spec, Sort.by(Sort.Direction.DESC, "id"));
    }

    @Transactional(readOnly = true)
    public T get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(resourceName, id));
    }

    @Transactional
    public T create(T entity) {
        entity.setId(null);
        beforeSave(entity, null);
        T saved = repository.save(entity);
        afterCreate(saved);
        return saved;
    }

    @Transactional
    public T update(Long id, T incoming) {
        T existing = get(id);
        T previous = BeanUtils.instantiateClass(type);
        BeanUtils.copyProperties(existing, previous);
        copyNonNull(incoming, existing);
        beforeSave(existing, previous);
        T saved = repository.save(existing);
        afterUpdate(saved, previous);
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        T existing = get(id);
        beforeDelete(existing);
        repository.delete(existing);
    }

    @Transactional
    public T updateStatus(Long id, StatusUpdateRequest request) {
        T entity = get(id);
        Field statusField = ReflectionUtils.findField(type, "status");
        if (statusField == null) {
            throw new BusinessException(resourceName + " does not support status transitions");
        }
        ReflectionUtils.makeAccessible(statusField);
        Object oldStatus = ReflectionUtils.getField(statusField, entity);
        Object newStatus = convert(request.status(), statusField.getType());
        validateTransition(entity, oldStatus, newStatus);
        ReflectionUtils.setField(statusField, entity, newStatus);
        BeanWrapper wrapper = new BeanWrapperImpl(entity);
        if (request.assignedTo() != null && !request.assignedTo().isBlank() && wrapper.isWritableProperty("assignedTo")) {
            wrapper.setPropertyValue("assignedTo", request.assignedTo());
        }
        onStatusChange(entity, oldStatus, newStatus, request);
        return repository.save(entity);
    }

    /* ---------------------------------------------------------------- hooks */

    /** Called before insert (previous == null) or update (previous = snapshot before merge). */
    protected void beforeSave(T entity, T previous) {
    }

    protected void afterCreate(T entity) {
    }

    protected void afterUpdate(T entity, T previous) {
    }

    protected void beforeDelete(T entity) {
    }

    protected void validateTransition(T entity, Object oldStatus, Object newStatus) {
    }

    protected void onStatusChange(T entity, Object oldStatus, Object newStatus, StatusUpdateRequest request) {
    }

    /* -------------------------------------------------------------- helpers */

    protected String resourceName() {
        return resourceName;
    }

    /** Builds a zero-padded business reference such as RES-00012. */
    protected static String reference(String prefix, Long id) {
        return String.format("%s-%05d", prefix, id);
    }

    private Specification<T> buildSpecification(Map<String, String> params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            params.forEach((key, value) -> {
                if (RESERVED_PARAMS.contains(key) || value == null || value.isBlank()) {
                    return;
                }
                Field field = ReflectionUtils.findField(type, key);
                if (field == null) {
                    return;
                }
                predicates.add(cb.equal(root.get(key), convert(value, field.getType())));
            });
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static Object convert(String value, Class<?> targetType) {
        if (targetType.isEnum()) {
            try {
                return Enum.valueOf((Class<Enum>) targetType, value.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BusinessException("Invalid value '" + value + "'. Allowed: "
                        + Arrays.toString(targetType.getEnumConstants()));
            }
        }
        return CONVERTER.convertValue(value, targetType);
    }

    private static void copyNonNull(Object source, Object target) {
        BeanWrapper src = new BeanWrapperImpl(source);
        List<String> ignore = new ArrayList<>(List.of("id", "createdAt", "updatedAt"));
        for (var pd : src.getPropertyDescriptors()) {
            if (src.isReadableProperty(pd.getName()) && src.getPropertyValue(pd.getName()) == null) {
                ignore.add(pd.getName());
            }
        }
        BeanUtils.copyProperties(source, target, ignore.toArray(String[]::new));
    }
}
