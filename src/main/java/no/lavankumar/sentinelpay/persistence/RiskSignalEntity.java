package no.lavankumar.sentinelpay.persistence;

import jakarta.persistence.*;

@Entity
@Table(name="risk_signal")
public class RiskSignalEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(name="transaction_id",nullable=false,length=64) public String transactionId;
    @Column(name="signal_code",nullable=false,length=40) public String code;
    @Column(nullable=false,length=160) public String title;
    @Column(nullable=false,length=500) public String detail;
    @Column(nullable=false) public int points;
    protected RiskSignalEntity() {}
    public RiskSignalEntity(String transactionId,String code,String title,String detail,int points) {
        this.transactionId=transactionId;this.code=code;this.title=title;this.detail=detail;this.points=points;
    }
}
