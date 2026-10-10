package com.healthharbor.water.service;

import com.healthharbor.water.entity.WaterQualityTest;
import com.healthharbor.water.entity.WaterSource.QualityStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WaterQualityEvaluatorTest {

    private final WaterQualityEvaluator evaluator = new WaterQualityEvaluator();

    private static WaterQualityTest sample(double ph, double turbidity, double tds, Double chlorine, boolean ecoli) {
        WaterQualityTest t = new WaterQualityTest();
        t.setPh(ph);
        t.setTurbidity(turbidity);
        t.setTds(tds);
        t.setResidualChlorine(chlorine);
        t.setEcoliPresent(ecoli);
        return t;
    }

    @Test
    void withinAcceptableLimitsIsSafe() {
        var result = evaluator.evaluate(sample(7.2, 0.6, 320, 0.4, false));
        assertThat(result.status()).isEqualTo(QualityStatus.SAFE);
    }

    @Test
    void permissibleButNotAcceptableIsMarginal() {
        var result = evaluator.evaluate(sample(7.0, 3.0, 900, 0.3, false));
        assertThat(result.status()).isEqualTo(QualityStatus.MARGINAL);
        assertThat(result.findings()).anyMatch(f -> f.startsWith("Turbidity")).anyMatch(f -> f.startsWith("TDS"));
    }

    @Test
    void bacteriologicalContaminationIsUnsafe() {
        var result = evaluator.evaluate(sample(7.0, 0.5, 300, 0.3, true));
        assertThat(result.status()).isEqualTo(QualityStatus.UNSAFE);
        assertThat(result.findings()).anyMatch(f -> f.contains("E. coli"));
    }

    @Test
    void fluorideBeyondPermissibleLimitIsUnsafe() {
        WaterQualityTest t = sample(7.5, 0.5, 400, null, false);
        t.setFluoride(1.8);
        assertThat(evaluator.evaluate(t).status()).isEqualTo(QualityStatus.UNSAFE);
    }

    @Test
    void lowResidualChlorineIsFlagged() {
        var result = evaluator.evaluate(sample(7.2, 0.5, 300, 0.05, false));
        assertThat(result.status()).isEqualTo(QualityStatus.MARGINAL);
        assertThat(result.findings()).anyMatch(f -> f.contains("inadequate disinfection"));
    }
}
