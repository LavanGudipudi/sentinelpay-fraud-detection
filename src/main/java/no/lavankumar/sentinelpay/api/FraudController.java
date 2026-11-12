package no.lavankumar.sentinelpay.api;

import no.lavankumar.sentinelpay.domain.Models.*;
import no.lavankumar.sentinelpay.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FraudController {
    private final TransactionService service;
    public FraudController(TransactionService service) { this.service=service; }
    @GetMapping("/transactions") public List<TransactionView> transactions() { return service.all(); }
    @GetMapping("/transactions/{id}") public TransactionView transaction(@PathVariable String id) { return service.get(id); }
    @PostMapping("/transactions/{id}/review") public TransactionView review(@PathVariable String id,@Valid @RequestBody ReviewRequest request) { return service.review(id,request); }
    @GetMapping("/summary") public DashboardSummary summary() { return service.summary(); }
    @GetMapping("/audit") public List<AuditEntry> audit() { return service.auditEntries(); }
}
