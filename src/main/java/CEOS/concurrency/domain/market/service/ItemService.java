package CEOS.concurrency.domain.market.service;

import CEOS.concurrency.common.code.BusinessErrorCode;
import CEOS.concurrency.common.exception.BusinessException;
import CEOS.concurrency.domain.market.dto.ItemResponse;
import java.util.List;
import CEOS.concurrency.domain.market.dto.PurchaseResponse;
import CEOS.concurrency.domain.market.dto.StatResponse;
import CEOS.concurrency.domain.market.entity.Item;
import CEOS.concurrency.domain.market.entity.Order;
import CEOS.concurrency.domain.market.repository.ItemRepository;
import CEOS.concurrency.domain.market.repository.OrderRepository;
import CEOS.concurrency.domain.member.entity.Member;
import CEOS.concurrency.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    public static final Long ITEM_ID = 1L;
    public static final int INITIAL_QUANTITY = 1000;

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<ItemResponse> getItems() {
        return itemRepository.findAll().stream()
                .map(ItemResponse::fromItem)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemResponse getItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.ITEM_NOT_FOUND));

        return ItemResponse.fromItem(item);
    }

    @Transactional(readOnly = true)
    public StatResponse getStat() {

        long purchaseAttempts = orderRepository.count();

        Map<String, Long> purchaseAttemptsByMember = orderRepository
                .countGroupByMember()
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));

        return new StatResponse(purchaseAttempts, purchaseAttemptsByMember);
    }

    @Transactional
    public PurchaseResponse purchase(Long itemId, UUID memberUuid) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.ITEM_NOT_FOUND));

        Member member = memberRepository.findByUuid(memberUuid)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.MEMBER_NOT_FOUND));

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        item.decreaseStock();
        orderRepository.save(Order.builder().item(item).member(member).build());
        return PurchaseResponse.from(item, member);
    }

    @Transactional
    public void reset() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
        itemRepository.insertItemWithId(INITIAL_QUANTITY);

    }
}
