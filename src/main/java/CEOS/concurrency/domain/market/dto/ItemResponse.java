package CEOS.concurrency.domain.market.dto;

import CEOS.concurrency.domain.market.entity.Item;

import java.util.Map;

public record ItemResponse(
        Long id,
        String name,
        int price,
        int quantity,
        long purchaseAttempts,
        Map<String, Long> purchaseAttemptsByMember
) {
    public static ItemResponse from(Item item, long purchaseAttempts, Map<String, Long> purchaseAttemptsByMember) {
        return new ItemResponse(item.getId(), item.getName(), item.getPrice(), item.getQuantity(), purchaseAttempts, purchaseAttemptsByMember);
    }
}
