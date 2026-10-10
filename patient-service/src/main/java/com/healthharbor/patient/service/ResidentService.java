package com.healthharbor.patient.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.patient.entity.Resident;
import com.healthharbor.patient.repository.ResidentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ResidentService extends CrudService<Resident> {

    public ResidentService(ResidentRepository repository) {
        super(repository, Resident.class, "Resident");
    }

    /** Supports free-text search across name, code, phone and village via ?q=. */
    @Override
    public List<Resident> list(Map<String, String> params) {
        List<Resident> all = super.list(params);
        String q = params == null ? null : params.get("q");
        if (q == null || q.isBlank()) {
            return all;
        }
        String needle = q.toLowerCase(Locale.ROOT);
        return all.stream().filter(r -> contains(r.getFullName(), needle) || contains(r.getResidentCode(), needle)
                || contains(r.getPhone(), needle) || contains(r.getVillage(), needle)).toList();
    }

    @Override
    protected void beforeSave(Resident entity, Resident previous) {
        if (entity.getAadhaarLast4() != null && entity.getAadhaarLast4().length() > 4) {
            String digits = entity.getAadhaarLast4().replaceAll("\\D", "");
            entity.setAadhaarLast4(digits.length() >= 4 ? digits.substring(digits.length() - 4) : digits);
        }
    }

    @Override
    protected void afterCreate(Resident entity) {
        if (entity.getResidentCode() == null || entity.getResidentCode().isBlank()) {
            entity.setResidentCode(reference("RES", entity.getId()));
            repository.save(entity);
        }
    }

    private static boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }
}
