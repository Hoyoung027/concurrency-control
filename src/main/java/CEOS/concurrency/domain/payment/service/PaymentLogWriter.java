package CEOS.concurrency.domain.payment.service;

import CEOS.concurrency.domain.payment.dto.PaymentResponse;
import CEOS.concurrency.domain.payment.entity.PaymentLog;
import CEOS.concurrency.domain.payment.entity.Store;
import CEOS.concurrency.domain.payment.enums.Currency;
import CEOS.concurrency.domain.payment.enums.PaymentStatus;
import CEOS.concurrency.domain.payment.repository.PaymentLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentLogWriter {

    private final PaymentLogRepository paymentLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentResponse save(String paymentId, Store store, String orderName,
                                Long amount, Currency currency, String customData,
                                PaymentStatus status) {
        PaymentLog paymentLog = paymentLogRepository.save(
                PaymentLog.builder()
                        .paymentId(paymentId)
                        .store(store)
                        .orderName(orderName)
                        .amount(amount)
                        .currency(currency)
                        .customData(customData)
                        .status(status)
                        .build()
        );
        return PaymentResponse.from(paymentLog);
    }
}
