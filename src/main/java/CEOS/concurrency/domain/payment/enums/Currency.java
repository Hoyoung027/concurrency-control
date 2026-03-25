package CEOS.concurrency.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {

    KRW("KRW"),
    USD("USD");

    private final String value;
}
