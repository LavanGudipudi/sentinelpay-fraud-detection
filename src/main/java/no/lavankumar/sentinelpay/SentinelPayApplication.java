package no.lavankumar.sentinelpay;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class SentinelPayApplication {
    public static void main(String[] args) { SpringApplication.run(SentinelPayApplication.class, args); }
}

enum RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }
enum ReviewStatus { PENDING, APPROVED, REJECTED, ESCALATED }

record Transaction(String id, String customer, BigDecimal amountNok, String merchant,
                   String country, int minutesSincePrevious, BigDecimal usualAmountNok,
                   boolean newDevice, Instant createdAt) {}
record RiskSignal(String code, String title, String detail, int points) {}
record RiskAssessment(int score, RiskLevel level, List<RiskSignal> signals) {}
record TransactionView(Transaction transaction, RiskAssessment assessment, ReviewStatus status,
                       String reviewerNote, Instant reviewedAt) {}
record ReviewRequest(@NotBlank String decision, @NotBlank String note, @NotBlank String reviewer) {}
record AuditEntry(String transactionId, String decision, String note, String reviewer, Instant timestamp) {}
record DashboardSummary(long total, long pending, long highRisk, long reviewed, BigDecimal flaggedAmountNok) {}

@Service
class RiskScoringService {
    RiskAssessment assess(Transaction tx) {
        List<RiskSignal> signals = new ArrayList<>();
        if (tx.amountNok().compareTo(tx.usualAmountNok().multiply(new BigDecimal("3"))) > 0) {
            signals.add(new RiskSignal("AMOUNT_SPIKE", "Amount is outside the usual pattern",
                    "This payment is more than 3x the customer's usual amount.", 35));
        }
        if (tx.minutesSincePrevious() <= 5) {
            signals.add(new RiskSignal("RAPID_ACTIVITY", "Several payments close together",
                    "The previous payment was recorded within the last five minutes.", 25));
        }
        if (tx.newDevice()) {
            signals.add(new RiskSignal("NEW_DEVICE", "First-seen device",
                    "The customer has not used this device in the demo history.", 20));
        }
        if (!"NO".equalsIgnoreCase(tx.country())) {
            signals.add(new RiskSignal("CROSS_BORDER", "Payment outside Norway",
                    "The transaction country differs from the customer's usual market (NO).", 15));
        }
        int score = Math.min(100, signals.stream().mapToInt(RiskSignal::points).sum());
        RiskLevel level = score >= 75 ? RiskLevel.CRITICAL : score >= 50 ? RiskLevel.HIGH
                : score >= 25 ? RiskLevel.MEDIUM : RiskLevel.LOW;
        return new RiskAssessment(score, level, List.copyOf(signals));
    }
}

@Service
class TransactionService {
    private final RiskScoringService scorer;
    private final Map<String, TransactionView> transactions = new ConcurrentHashMap<>();
    private final List<AuditEntry> audit = Collections.synchronizedList(new ArrayList<>());

    TransactionService(RiskScoringService scorer) {
        this.scorer = scorer;
        seed();
    }

    private void seed() {
        add(new Transaction("SP-1042", "Kari Nilsen", new BigDecimal("48500"), "Nordic Electronics", "NO", 2, new BigDecimal("850"), true, Instant.now().minusSeconds(240)));
        add(new Transaction("SP-1041", "Ola Berg", new BigDecimal("1290"), "Oslo Coffee Roasters", "NO", 180, new BigDecimal("1100"), false, Instant.now().minusSeconds(900)));
        add(new Transaction("SP-1039", "Mina Hansen", new BigDecimal("9200"), "Travel Market", "SE", 4, new BigDecimal("1400"), true, Instant.now().minusSeconds(1800)));
        add(new Transaction("SP-1037", "Erik Johansen", new BigDecimal("640"), "City Transit", "NO", 65, new BigDecimal("520"), false, Instant.now().minusSeconds(3600)));
        add(new Transaction("SP-1032", "Sara Lund", new BigDecimal("18700"), "Digital Goods Hub", "DK", 3, new BigDecimal("2100"), true, Instant.now().minusSeconds(5400)));
        add(new Transaction("SP-1028", "Jonas Dahl", new BigDecimal("2450"), "Grocery Market", "NO", 240, new BigDecimal("1800"), false, Instant.now().minusSeconds(7200)));
    }

    private void add(Transaction tx) {
        transactions.put(tx.id(), new TransactionView(tx, scorer.assess(tx), ReviewStatus.PENDING, "", null));
    }

    List<TransactionView> all() {
        return transactions.values().stream()
                .sorted(Comparator.comparing(v -> v.transaction().createdAt(), Comparator.reverseOrder())).toList();
    }

    TransactionView get(String id) {
        TransactionView view = transactions.get(id);
        if (view == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found");
        return view;
    }

    synchronized TransactionView review(String id, ReviewRequest request) {
        TransactionView current = get(id);
        ReviewStatus decision;
        try { decision = ReviewStatus.valueOf(request.decision().trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision must be APPROVED, REJECTED or ESCALATED"); }
        if (decision == ReviewStatus.PENDING) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PENDING is not a review decision");
        if (current.status() != ReviewStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This case has already been reviewed");
        Instant now = Instant.now();
        TransactionView updated = new TransactionView(current.transaction(), current.assessment(), decision, request.note().trim(), now);
        transactions.put(id, updated);
        audit.add(new AuditEntry(id, decision.name(), request.note().trim(), request.reviewer().trim(), now));
        return updated;
    }

    List<AuditEntry> audit() { synchronized (audit) { return List.copyOf(audit); } }

    DashboardSummary summary() {
        List<TransactionView> all = all();
        long high = all.stream().filter(v -> v.assessment().score() >= 50).count();
        long reviewed = all.stream().filter(v -> v.status() != ReviewStatus.PENDING).count();
        BigDecimal flagged = all.stream().filter(v -> v.assessment().score() >= 50)
                .map(v -> v.transaction().amountNok()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardSummary(all.size(), all.stream().filter(v -> v.status() == ReviewStatus.PENDING).count(),
                high, reviewed, flagged);
    }
}

@RestController
@RequestMapping("/api")
class FraudController {
    private final TransactionService service;
    FraudController(TransactionService service) { this.service = service; }

    @GetMapping("/transactions") List<TransactionView> transactions() { return service.all(); }
    @GetMapping("/transactions/{id}") TransactionView transaction(@PathVariable String id) { return service.get(id); }
    @PostMapping("/transactions/{id}/review")
    TransactionView review(@PathVariable String id, @Valid @RequestBody ReviewRequest request) { return service.review(id, request); }
    @GetMapping("/summary") DashboardSummary summary() { return service.summary(); }
    @GetMapping("/audit") List<AuditEntry> audit() { return service.audit(); }
}
