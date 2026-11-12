package no.lavankumar.sentinelpay.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

interface TransactionRepository extends JpaRepository<TransactionEntity,String> {}
interface FraudCaseRepository extends JpaRepository<FraudCaseEntity,String> {
    List<FraudCaseEntity> findAllByOrderByTransactionIdDesc();
}
interface RiskSignalRepository extends JpaRepository<RiskSignalEntity,Long> {
    List<RiskSignalEntity> findByTransactionIdOrderById(String transactionId);
}
interface ReviewAuditRepository extends JpaRepository<ReviewAuditEntity,Long> {
    List<ReviewAuditEntity> findAllByOrderByCreatedAtDesc();
}
