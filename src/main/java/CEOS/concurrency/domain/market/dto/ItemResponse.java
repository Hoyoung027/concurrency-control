package CEOS.concurrency.domain.market.dto;

import CEOS.concurrency.domain.market.entity.Item;

import java.util.Map;

public record ItemResponse(
        Long id,
        String name,
        int price,
        int stock
) {
    public static ItemResponse fromItem(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getStock()
        );
    }
}
