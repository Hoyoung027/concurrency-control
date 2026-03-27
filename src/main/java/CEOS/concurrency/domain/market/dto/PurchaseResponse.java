package CEOS.concurrency.domain.market.dto;

import CEOS.concurrency.domain.market.entity.Item;
import CEOS.concurrency.domain.member.entity.Member;

import java.util.UUID;

public record PurchaseResponse(
        String itemName,
        int remainingStock,
        UUID memberUuid,
        String memberNickname
) {
    public static PurchaseResponse from(Item item, Member member) {
        return new PurchaseResponse(item.getName(), item.getStock(), member.getUuid(), member.getNickname());
    }
}
