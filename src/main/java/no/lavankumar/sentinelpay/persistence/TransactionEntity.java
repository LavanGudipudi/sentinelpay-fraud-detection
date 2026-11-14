package no.lavankumar.sentinelpay.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="payment_transaction")
public class TransactionEntity {
    @Id @Column(length=64) public String id;
    @Column(name="customer_name",nullable=false,length=160) public String customer;
    @Column(name="amount_nok",nullable=false,precision=19,scale=2) public BigDecimal amountNok;
    @Column(nullable=false,length=160) public String merchant;
    @Column(nullable=false,length=2) public String country;
    @Column(name="minutes_since_previous",nullable=false) public int minutesSincePrevious;
    @Column(name="usual_amount_nok",nullable=false,precision=19,scale=2) public BigDecimal usualAmountNok;
    @Column(name="new_device",nullable=false) public boolean newDevice;
    @Column(name="created_at",nullable=false) public Instant createdAt;
    protected TransactionEntity() {}
    public TransactionEntity(String id,String customer,BigDecimal amountNok,String merchant,String country,
            int minutesSincePrevious,BigDecimal usualAmountNok,boolean newDevice,Instant createdAt) {
        this.id=id;this.customer=customer;this.amountNok=amountNok;this.merchant=merchant;this.country=country;
        this.minutesSincePrevious=minutesSincePrevious;this.usualAmountNok=usualAmountNok;
        this.newDevice=newDevice;this.createdAt=createdAt;
    }
}
