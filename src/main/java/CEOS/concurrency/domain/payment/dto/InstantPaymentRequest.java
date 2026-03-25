package CEOS.concurrency.domain.payment.dto;

import CEOS.concurrency.domain.payment.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InstantPaymentRequest(
        @NotBlank(message = "가맹점 ID는 필수입니다") String storeId,
        @NotBlank(message = "주문명은 필수입니다") String orderName,
        @NotNull(message = "결제 금액은 필수입니다") @Positive(message = "결제 금액은 0보다 커야 합니다") Integer totalPayAmount,
        @NotNull(message = "통화는 필수입니다") Currency currency,
        String customData
) {
}
