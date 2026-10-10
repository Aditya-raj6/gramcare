package com.healthharbor.water.service;

import com.healthharbor.water.entity.WaterQualityTest;
import com.healthharbor.water.entity.WaterSource.QualityStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Classifies a water sample against BIS 10500:2012 drinking-water limits.
 * SAFE = within acceptable limits, MARGINAL = within permissible limits (no alternate source),
 * UNSAFE = beyond permissible limits or bacteriological contamination.
 */
@Component
public class WaterQualityEvaluator {

    public record Evaluation(QualityStatus status, List<String> findings) {
    }

    public Evaluation evaluate(WaterQualityTest t) {
        List<String> marginal = new ArrayList<>();
        List<String> unsafe = new ArrayList<>();

        if (Boolean.TRUE.equals(t.getEcoliPresent())) {
            unsafe.add("E. coli detected (must be absent)");
        }
        if (Boolean.TRUE.equals(t.getColiformPresent())) {
            unsafe.add("Total coliform detected (must be absent)");
        }
        if (t.getPh() != null) {
            if (t.getPh() < 6.0 || t.getPh() > 9.0) {
                unsafe.add("pH " + t.getPh() + " far outside 6.5-8.5");
            } else if (t.getPh() < 6.5 || t.getPh() > 8.5) {
                marginal.add("pH " + t.getPh() + " outside 6.5-8.5");
            }
        }
        band(t.getTurbidity(), 1, 5, "Turbidity", "NTU", marginal, unsafe);
        band(t.getTds(), 500, 2000, "TDS", "mg/L", marginal, unsafe);
        band(t.getFluoride(), 1.0, 1.5, "Fluoride", "mg/L", marginal, unsafe);
        band(t.getNitrate(), 45, 45, "Nitrate", "mg/L", marginal, unsafe);
        band(t.getArsenic(), 0.01, 0.01, "Arsenic", "mg/L", marginal, unsafe);
        band(t.getIron(), 1.0, 1.0, "Iron", "mg/L", marginal, unsafe);
        if (t.getResidualChlorine() != null) {
            if (t.getResidualChlorine() < 0.2) {
                marginal.add("Residual chlorine " + t.getResidualChlorine() + " mg/L below 0.2 (inadequate disinfection)");
            } else if (t.getResidualChlorine() > 1.0) {
                marginal.add("Residual chlorine " + t.getResidualChlorine() + " mg/L above 1.0");
            }
        }

        List<String> findings = new ArrayList<>(unsafe);
        findings.addAll(marginal);
        QualityStatus status = !unsafe.isEmpty() ? QualityStatus.UNSAFE
                : !marginal.isEmpty() ? QualityStatus.MARGINAL : QualityStatus.SAFE;
        if (findings.isEmpty()) {
            findings.add("All tested parameters within acceptable limits");
        }
        return new Evaluation(status, findings);
    }

    private static void band(Double value, double acceptable, double permissible, String name, String unit,
                             List<String> marginal, List<String> unsafe) {
        if (value == null) {
            return;
        }
        if (value > permissible) {
            unsafe.add(name + " " + value + " " + unit + " exceeds permissible limit " + permissible);
        } else if (value > acceptable) {
            marginal.add(name + " " + value + " " + unit + " above acceptable limit " + acceptable);
        }
    }
}
