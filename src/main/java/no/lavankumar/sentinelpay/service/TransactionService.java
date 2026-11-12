package no.lavankumar.sentinelpay.service;

import no.lavankumar.sentinelpay.api.ReviewRequest;
import no.lavankumar.sentinelpay.domain.Models.*;
import no.lavankumar.sentinelpay.domain.RiskScoringService;
import no.lavankumar.sentinelpay.persistence.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class TransactionService {
    private final TransactionRepository transactions;
    private final FraudCaseRepository cases;
    private final RiskSignalRepository signals;
    private final ReviewAuditRepository audit;

    public TransactionService(TransactionRepository transactions,FraudCaseRepository cases,
            RiskSignalRepository signals,ReviewAuditRepository audit,RiskScoringService scorer) {
        this.transactions=transactions;this.cases=cases;this.signals=signals;this.audit=audit;
    }

    @Transactional(readOnly=true)
    public List<TransactionView> all() { return cases.findAll().stream().map(c->view(c.transactionId)).toList(); }

    @Transactional(readOnly=true)
    public TransactionView get(String id) { return view(id); }

    private TransactionView view(String id) {
        TransactionEntity t=transactions.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Transaction not found"));
        FraudCaseEntity c=cases.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Case not found"));
        List<RiskSignal> reasons=signals.findByTransactionIdOrderById(id).stream().map(s->new RiskSignal(s.code,s.title,s.detail,s.points)).toList();
        Transaction tx=new Transaction(t.id,t.customer,t.amountNok,t.merchant,t.country,t.minutesSincePrevious,t.usualAmountNok,t.newDevice,t.createdAt);
        return new TransactionView(tx,new RiskAssessment(c.riskScore,c.riskLevel,reasons),c.reviewStatus,c.reviewerNote,c.reviewer,c.reviewedAt);
    }

    @Transactional
    public TransactionView review(String id,ReviewRequest request) {
        String note=request.note().trim(), reviewer=request.reviewer().trim();
        ReviewStatus decision;
        try { decision=ReviewStatus.valueOf(request.decision().trim().toUpperCase(Locale.ROOT)); }
        catch(IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Decision must be APPROVED, REJECTED or ESCALATED"); }
        if(decision==ReviewStatus.PENDING) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"PENDING is not a review decision");
        FraudCaseEntity c=cases.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Case not found"));
        if(c.reviewStatus!=ReviewStatus.PENDING) throw new ResponseStatusException(HttpStatus.CONFLICT,"This case has already been reviewed");
        Instant now=Instant.now();
        c.reviewStatus=decision;c.reviewerNote=note;c.reviewer=reviewer;c.reviewedAt=now;
        cases.saveAndFlush(c);
        audit.save(new ReviewAuditEntity(id,decision.name(),note,reviewer,now));
        return view(id);
    }

    @Transactional(readOnly=true)
    public List<AuditEntry> auditEntries() {
        return audit.findAllByOrderByCreatedAtDesc().stream().map(a->new AuditEntry(a.transactionId,a.decision,a.note,a.reviewer,a.createdAt)).toList();
    }

    @Transactional(readOnly=true)
    public DashboardSummary summary() {
        List<TransactionView> all=all();
        long high=all.stream().filter(v->v.assessment().score()>=50).count();
        long reviewed=all.stream().filter(v->v.status()!=ReviewStatus.PENDING).count();
        BigDecimal flagged=all.stream().filter(v->v.assessment().score()>=50).map(v->v.transaction().amountNok()).reduce(BigDecimal.ZERO,BigDecimal::add);
        return new DashboardSummary(all.size(),all.stream().filter(v->v.status()==ReviewStatus.PENDING).count(),high,reviewed,flagged);
    }
}
