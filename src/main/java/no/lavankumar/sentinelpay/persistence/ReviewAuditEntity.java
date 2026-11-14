package no.lavankumar.sentinelpay.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="review_audit")
public class ReviewAuditEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(name="transaction_id",nullable=false,length=64) public String transactionId;
    @Column(nullable=false,length=16) public String decision;
    @Column(nullable=false,length=500) public String note;
    @Column(nullable=false,length=80) public String reviewer;
    @Column(name="created_at",nullable=false) public Instant createdAt;
    protected ReviewAuditEntity() {}
    public ReviewAuditEntity(String transactionId,String decision,String note,String reviewer,Instant createdAt) {
        this.transactionId=transactionId;this.decision=decision;this.note=note;this.reviewer=reviewer;this.createdAt=createdAt;
    }
}
