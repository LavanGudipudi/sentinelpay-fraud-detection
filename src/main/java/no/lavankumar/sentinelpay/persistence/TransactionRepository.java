package no.lavankumar.sentinelpay.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TransactionRepository extends JpaRepository<TransactionEntity,String> {}
