package CEOS.concurrency.domain.market.service;

import CEOS.concurrency.common.code.BusinessErrorCode;
import CEOS.concurrency.common.exception.BusinessException;
import CEOS.concurrency.domain.market.dto.ItemResponse;
import CEOS.concurrency.domain.market.dto.PurchaseResponse;
import CEOS.concurrency.domain.market.entity.Item;
import CEOS.concurrency.domain.market.entity.PurchaseLog;
import CEOS.concurrency.domain.market.repository.ItemRepository;
import CEOS.concurrency.domain.market.repository.PurchaseLogRepository;
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
    private final PurchaseLogRepository purchaseLogRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public ItemResponse getItem() {
        Item item = itemRepository.findById(ITEM_ID)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.ITEM_NOT_FOUND));

        long purchaseAttempts = purchaseLogRepository.countByItem(item);

        Map<String, Long> purchaseAttemptsByMember = purchaseLogRepository
                .countByItemGroupByMember(item)
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));

        return ItemResponse.from(item, purchaseAttempts, purchaseAttemptsByMember);
    }

    @Transactional
    public PurchaseResponse purchase(UUID memberUuid) {
        Item item = itemRepository.findById(ITEM_ID)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.ITEM_NOT_FOUND));

        if (item.getQuantity() <= 0) {
            throw new BusinessException(BusinessErrorCode.OUT_OF_STOCK);
        }

        Member member = memberRepository.findByUuid(memberUuid)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.MEMBER_NOT_FOUND));

        purchaseLogRepository.save(PurchaseLog.builder().item(item).member(member).build());
        item.decreaseQuantity();
        return PurchaseResponse.from(item);
    }

    @Transactional
    public void reset() {
        purchaseLogRepository.deleteAll();
        itemRepository.deleteAll();
        itemRepository.insertItemWithId(INITIAL_QUANTITY);
    }
}
