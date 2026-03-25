package CEOS.concurrency.domain.payment.dto;

import CEOS.concurrency.domain.payment.enums.Currency;
import CEOS.concurrency.domain.payment.enums.PaymentStatus;

import CEOS.concurrency.domain.payment.entity.PaymentLog;

import java.time.LocalDateTime;

public record PaymentResponse(
        String paymentId,
        PaymentStatus paymentStatus,
        String orderName,
        String pgProvider,
        Currency currency,
        String customData,
        LocalDateTime paidAt
) {
    public static PaymentResponse from(PaymentLog paymentLog) {
        return new PaymentResponse(
                paymentLog.getPaymentId(),
                paymentLog.getStatus(),
                paymentLog.getOrderName(),
                paymentLog.getPgProvider(),
                paymentLog.getCurrency(),
                paymentLog.getCustomData(),
                paymentLog.getCreatedAt()
        );
    }
}
