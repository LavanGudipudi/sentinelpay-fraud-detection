package no.lavankumar.sentinelpay.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class Models {
    private Models() {}
    public enum RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }
    public enum ReviewStatus { PENDING, APPROVED, REJECTED, ESCALATED }
    public record Transaction(String id, String customer, BigDecimal amountNok, String merchant,
                              String country, int minutesSincePrevious, BigDecimal usualAmountNok,
                              boolean newDevice, Instant createdAt) {}
    public record RiskSignal(String code, String title, String detail, int points) {}
    public record RiskAssessment(int score, RiskLevel level, List<RiskSignal> signals) {}
    public record TransactionView(Transaction transaction, RiskAssessment assessment, ReviewStatus status,
                                  String reviewerNote, String reviewer, Instant reviewedAt) {}
    public record ReviewRequest(String decision, String note, String reviewer) {}
    public record AuditEntry(String transactionId, String decision, String note, String reviewer, Instant timestamp) {}
    public record DashboardSummary(long total, long pending, long highRisk, long reviewed, BigDecimal flaggedAmountNok) {}
}
