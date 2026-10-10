package com.gramcare.resident.service;

import com.gramcare.resident.dto.HealthCentreRequest;
import com.gramcare.resident.entity.*;
import com.gramcare.resident.exception.ResourceNotFoundException;
import com.gramcare.resident.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HealthCentreService {

    private final HealthCentreRepository healthCentreRepository;
    private final HealthcareWorkerRepository workerRepository;
    private final CentreServiceRepository centreServiceRepository;
    private final OperatingScheduleRepository scheduleRepository;

    public HealthCentreService(HealthCentreRepository healthCentreRepository,
                                HealthcareWorkerRepository workerRepository,
                                CentreServiceRepository centreServiceRepository,
                                OperatingScheduleRepository scheduleRepository) {
        this.healthCentreRepository = healthCentreRepository;
        this.workerRepository = workerRepository;
        this.centreServiceRepository = centreServiceRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Transactional(readOnly = true)
    public List<HealthCentre> findAll() { return healthCentreRepository.findAll(); }

    @Transactional(readOnly = true)
    public HealthCentre findById(Long id) {
        return healthCentreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health centre not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<HealthCentre> search(String name, String village) {
        if (name != null && !name.isBlank()) return healthCentreRepository.findByNameContainingIgnoreCase(name);
        if (village != null && !village.isBlank()) return healthCentreRepository.findByVillageIgnoreCase(village);
        return healthCentreRepository.findAll();
    }

    public HealthCentre create(HealthCentreRequest req) {
        HealthCentre hc = new HealthCentre();
        apply(hc, req);
        return healthCentreRepository.save(hc);
    }

    public HealthCentre update(Long id, HealthCentreRequest req) {
        HealthCentre hc = findById(id);
        apply(hc, req);
        return healthCentreRepository.save(hc);
    }

    private void apply(HealthCentre hc, HealthCentreRequest req) {
        hc.setName(req.getName());
        hc.setVillage(req.getVillage());
        hc.setAddress(req.getAddress());
        hc.setContactPhone(req.getContactPhone());
        hc.setContactEmail(req.getContactEmail());
        if (req.getCentreType() != null) hc.setCentreType(req.getCentreType());
    }

    public HealthcareWorker addWorker(Long centreId, HealthcareWorker worker) {
        HealthCentre hc = findById(centreId);
        worker.setHealthCentre(hc);
        return workerRepository.save(worker);
    }

    @Transactional(readOnly = true)
    public List<HealthcareWorker> getWorkers(Long centreId) {
        return workerRepository.findByHealthCentreId(centreId);
    }

    public CentreService addService(Long centreId, CentreService service) {
        HealthCentre hc = findById(centreId);
        service.setHealthCentre(hc);
        return centreServiceRepository.save(service);
    }

    @Transactional(readOnly = true)
    public List<CentreService> getServices(Long centreId) {
        return centreServiceRepository.findByHealthCentreId(centreId);
    }

    public OperatingSchedule addSchedule(Long centreId, OperatingSchedule schedule) {
        HealthCentre hc = findById(centreId);
        schedule.setHealthCentre(hc);
        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public List<OperatingSchedule> getSchedules(Long centreId) {
        return scheduleRepository.findByHealthCentreId(centreId);
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return healthCentreRepository.findByActiveTrue().size();
    }
}
