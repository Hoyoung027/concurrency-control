package CEOS.concurrency.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    PAID("PAID"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED");

    private final String value;
}
