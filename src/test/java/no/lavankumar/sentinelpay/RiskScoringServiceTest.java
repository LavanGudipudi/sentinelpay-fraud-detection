package no.lavankumar.sentinelpay;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class RiskScoringServiceTest {
    private final RiskScoringService scorer = new RiskScoringService();

    @Test void addsSignalsForUnusualAmountRapidActivityAndNewDevice() {
        Transaction tx = new Transaction("T-1", "Demo Customer", new BigDecimal("10000"), "Demo Shop",
                "NO", 2, new BigDecimal("500"), true, Instant.now());
        RiskAssessment result = scorer.assess(tx);
        assertEquals(80, result.score());
        assertEquals(RiskLevel.CRITICAL, result.level());
        assertEquals(3, result.signals().size());
    }

    @Test void leavesOrdinaryDomesticPaymentAtLowRisk() {
        Transaction tx = new Transaction("T-2", "Demo Customer", new BigDecimal("400"), "Local Shop",
                "NO", 90, new BigDecimal("500"), false, Instant.now());
        RiskAssessment result = scorer.assess(tx);
        assertEquals(0, result.score());
        assertEquals(RiskLevel.LOW, result.level());
        assertTrue(result.signals().isEmpty());
    }

    @Test void combinesAllFourSignalsIntoCriticalRiskScore() {
        Transaction tx = new Transaction("T-3", "Demo Customer", new BigDecimal("20000"), "Online Shop",
                "SE", 1, new BigDecimal("100"), true, Instant.now());
        assertEquals(95, scorer.assess(tx).score());
    }
}
