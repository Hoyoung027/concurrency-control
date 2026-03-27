package CEOS.concurrency.domain.market.dto;

import CEOS.concurrency.domain.market.entity.Item;

import java.util.Map;

public record StatResponse(
        long purchaseAttempts,
        Map<String, Long> purchaseAttemptsByMember
) {
}
