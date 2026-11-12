package no.lavankumar.sentinelpay.domain;

import no.lavankumar.sentinelpay.domain.Models.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class RiskScoringService {
    public RiskAssessment assess(Transaction tx) {
        List<RiskSignal> signals = new ArrayList<>();
        if (tx.amountNok().compareTo(tx.usualAmountNok().multiply(new BigDecimal("3"))) > 0)
            signals.add(new RiskSignal("AMOUNT_SPIKE", "Amount is outside the usual pattern",
                    "This payment is more than 3x the customer's usual amount.", 35));
        if (tx.minutesSincePrevious() <= 5)
            signals.add(new RiskSignal("RAPID_ACTIVITY", "Several payments close together",
                    "The previous payment was recorded within the last five minutes.", 25));
        if (tx.newDevice())
            signals.add(new RiskSignal("NEW_DEVICE", "First-seen device",
                    "The customer has not used this device in the demo history.", 20));
        if (!"NO".equalsIgnoreCase(tx.country()))
            signals.add(new RiskSignal("CROSS_BORDER", "Payment outside Norway",
                    "The transaction country differs from the demo home market (NO).", 15));
        int score = Math.min(100, signals.stream().mapToInt(RiskSignal::points).sum());
        RiskLevel level = score >= 75 ? RiskLevel.CRITICAL : score >= 50 ? RiskLevel.HIGH
                : score >= 25 ? RiskLevel.MEDIUM : RiskLevel.LOW;
        return new RiskAssessment(score, level, List.copyOf(signals));
    }
}
