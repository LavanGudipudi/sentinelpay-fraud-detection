package no.lavankumar.sentinelpay;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import no.lavankumar.sentinelpay.domain.Models.*;
import no.lavankumar.sentinelpay.domain.RiskScoringService;
import org.junit.jupiter.api.Test;

class RiskScoringServiceTest {
    private final RiskScoringService scorer = new RiskScoringService();

    @Test
    void addsSignalsForUnusualAmountRapidActivityAndNewDevice() {
        Transaction tx = transaction("T-1", "10000", "500", "NO", 2, true);
        RiskAssessment result = scorer.assess(tx);

        assertEquals(80, result.score());
        assertEquals(RiskLevel.CRITICAL, result.level());
        assertEquals(3, result.signals().size());
    }

    @Test
    void leavesOrdinaryDomesticPaymentAtLowRisk() {
        Transaction tx = transaction("T-2", "400", "500", "NO", 90, false);
        RiskAssessment result = scorer.assess(tx);

        assertEquals(0, result.score());
        assertEquals(RiskLevel.LOW, result.level());
        assertTrue(result.signals().isEmpty());
    }

    @Test
    void combinesAllFourSignalsAndCapsScoreAtOneHundred() {
        Transaction tx = transaction("T-3", "20000", "100", "SE", 1, true);
        RiskAssessment result = scorer.assess(tx);

        assertEquals(95, result.score());
        assertEquals(4, result.signals().size());
    }

    @Test
    void amountExactlyThreeTimesUsualDoesNotTriggerAmountSpike() {
        RiskAssessment result = scorer.assess(transaction("T-4", "1500", "500", "NO", 90, false));

        assertFalse(result.signals().stream().anyMatch(s -> s.code().equals("AMOUNT_SPIKE")));
        assertEquals(0, result.score());
    }

    @Test
    void rapidActivityBoundaryIncludesExactlyFiveMinutes() {
        RiskAssessment atBoundary = scorer.assess(transaction("T-5", "500", "500", "NO", 5, false));
        RiskAssessment outsideBoundary = scorer.assess(transaction("T-6", "500", "500", "NO", 6, false));

        assertTrue(atBoundary.signals().stream().anyMatch(s -> s.code().equals("RAPID_ACTIVITY")));
        assertFalse(outsideBoundary.signals().stream().anyMatch(s -> s.code().equals("RAPID_ACTIVITY")));
    }

    @Test
    void countryComparisonIsCaseInsensitiveForNorway() {
        RiskAssessment result = scorer.assess(transaction("T-7", "500", "500", "no", 90, false));

        assertFalse(result.signals().stream().anyMatch(s -> s.code().equals("CROSS_BORDER")));
    }

    private Transaction transaction(String id, String amount, String usualAmount, String country,
            int minutesSincePrevious, boolean newDevice) {
        return new Transaction(id, "Demo Customer", new BigDecimal(amount), "Demo Shop", country,
                minutesSincePrevious, new BigDecimal(usualAmount), newDevice, Instant.parse("2026-01-01T10:00:00Z"));
    }
}
