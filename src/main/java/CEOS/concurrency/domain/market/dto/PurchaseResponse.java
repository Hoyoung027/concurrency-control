package CEOS.concurrency.domain.market.dto;

import CEOS.concurrency.domain.market.entity.Item;

public record PurchaseResponse(
        String name,
        int remainingQuantity
) {
    public static PurchaseResponse from(Item item) {
        return new PurchaseResponse(item.getName(), item.getQuantity());
    }
}
