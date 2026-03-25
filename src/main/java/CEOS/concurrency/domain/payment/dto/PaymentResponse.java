package CEOS.concurrency.domain.payment.dto;

import CEOS.concurrency.domain.payment.entity.PaymentLog;

import java.time.LocalDateTime;

public record PaymentResponse(
        String paymentId,
        LocalDateTime paidAt
) {
    public static PaymentResponse from(PaymentLog paymentLog) {
        return new PaymentResponse(
                paymentLog.getPaymentId(),
                paymentLog.getCreatedAt()
        );
    }
}
