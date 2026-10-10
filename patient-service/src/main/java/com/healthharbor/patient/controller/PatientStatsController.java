package com.healthharbor.patient.controller;

import com.healthharbor.patient.repository.MedicalRecordRepository;
import com.healthharbor.patient.repository.ResidentRepository;
import com.healthharbor.patient.repository.VisitRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Patient statistics")
public class PatientStatsController {

    private final ResidentRepository residents;
    private final VisitRepository visits;
    private final MedicalRecordRepository records;

    @GetMapping("/api/residents/stats")
    public Map<String, Object> stats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalResidents", residents.count());
        stats.put("chronicPatients", residents.countByChronicConditionsIsNotNullAndChronicConditionsNot(""));
        stats.put("pregnantWomen", residents.countByPregnantTrue());
        stats.put("totalVisits", visits.count());
        stats.put("visitsLast30Days", visits.countByVisitDateGreaterThanEqual(LocalDate.now().minusDays(30)));
        stats.put("medicalRecords", records.count());
        stats.put("byVillage", toMap(residents.countByVillage()));
        stats.put("byGender", toMap(residents.countByGender()));
        stats.put("visitsByType", toMap(visits.countByType()));
        stats.put("dailyVisits", toMap(visits.dailyCounts(LocalDate.now().minusDays(30))));
        return stats;
    }

    static Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        rows.forEach(r -> map.put(String.valueOf(r[0]), ((Number) r[1]).longValue()));
        return map;
    }
}
