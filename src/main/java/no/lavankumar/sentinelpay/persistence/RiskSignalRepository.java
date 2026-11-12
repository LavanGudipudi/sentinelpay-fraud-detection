package no.lavankumar.sentinelpay.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RiskSignalRepository extends JpaRepository<RiskSignalEntity,Long> {
    List<RiskSignalEntity> findByTransactionIdOrderById(String transactionId);
}
