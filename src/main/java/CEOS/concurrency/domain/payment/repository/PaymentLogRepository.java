package CEOS.concurrency.domain.payment.repository;

import CEOS.concurrency.domain.payment.entity.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    long countByPaymentIdStartingWith(String prefix);

    boolean existsByStore_IdAndPaymentId(Long storeId, String paymentId);
}
