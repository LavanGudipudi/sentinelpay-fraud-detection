package no.lavankumar.sentinelpay.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ReviewAuditRepository extends JpaRepository<ReviewAuditEntity,Long> {
    List<ReviewAuditEntity> findAllByOrderByCreatedAtDesc();
}
