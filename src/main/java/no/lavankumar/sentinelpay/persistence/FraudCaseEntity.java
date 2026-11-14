package no.lavankumar.sentinelpay.persistence;

import jakarta.persistence.*;
import no.lavankumar.sentinelpay.domain.Models.*;
import java.time.Instant;

@Entity
@Table(name="fraud_case")
public class FraudCaseEntity {
    @Id @Column(name="transaction_id",length=64) public String transactionId;
    @Column(name="risk_score",nullable=false) public int riskScore;
    @Enumerated(EnumType.STRING) @Column(name="risk_level",nullable=false,length=16) public RiskLevel riskLevel;
    @Enumerated(EnumType.STRING) @Column(name="review_status",nullable=false,length=16) public ReviewStatus reviewStatus;
    @Column(name="reviewer_note",length=500) public String reviewerNote;
    @Column(length=80) public String reviewer;
    @Column(name="reviewed_at") public Instant reviewedAt;
    @Version public long version;
    protected FraudCaseEntity() {}
    public FraudCaseEntity(String id,int score,RiskLevel level) {
        transactionId=id;riskScore=score;riskLevel=level;reviewStatus=ReviewStatus.PENDING;reviewerNote="";
    }
}
