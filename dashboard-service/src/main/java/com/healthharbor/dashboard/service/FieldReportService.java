package com.healthharbor.dashboard.service;

import com.healthharbor.common.exception.BusinessException;
import com.healthharbor.common.exception.ResourceNotFoundException;
import com.healthharbor.dashboard.entity.FieldReport;
import com.healthharbor.dashboard.entity.FieldReportItem;
import com.healthharbor.dashboard.repository.FieldReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldReportService {

    private final FieldReportRepository repository;
    private final FieldReportRouter router;

    @Transactional
    public List<FieldReport> list() {
        List<FieldReport> reports = repository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        reports.forEach(r -> r.getItems().forEach(router::sync));
        return reports;
    }

    @Transactional
    public FieldReport get(Long id) {
        FieldReport report = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Field report", id));
        report.getItems().forEach(router::sync);
        return report;
    }

    /** Records the report, then routes every item to its owning service. */
    @Transactional
    public FieldReport submit(FieldReport report, String user, String role) {
        report.setId(null);
        if (report.getReportedBy() == null || report.getReportedBy().isBlank()) {
            report.setReportedBy(user);
        }
        report.setReporterRole(role);
        if (report.getReportDate() == null) {
            report.setReportDate(LocalDate.now());
        }
        report.getItems().forEach(item -> {
            validate(item);
            item.setId(null);
            item.setReport(report);
        });
        FieldReport saved = repository.save(report);
        saved.setReportNumber(String.format("FR-%05d", saved.getId()));
        saved.getItems().forEach(item -> router.route(saved, item));
        return repository.save(saved);
    }

    @Transactional
    public FieldReport retry(Long reportId) {
        FieldReport report = get(reportId);
        report.getItems().stream().filter(i -> i.getRoutingStatus() == FieldReportItem.RoutingStatus.FAILED)
                .forEach(i -> router.route(report, i));
        return repository.save(report);
    }

    private static void validate(FieldReportItem item) {
        switch (item.getItemType()) {
            case SANITATION_MAINTENANCE -> require(item.getFacilityId() != null, "Select the sanitation facility that needs maintenance");
            case MEDICINE_SHORTAGE -> require(item.getMedicineId() != null && item.getHealthCentreId() != null,
                    "Medicine shortage requires a medicine and a health centre");
            default -> {
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new BusinessException(message);
        }
    }
}
