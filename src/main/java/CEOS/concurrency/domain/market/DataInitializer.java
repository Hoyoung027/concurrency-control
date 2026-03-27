package CEOS.concurrency.domain.market;

import CEOS.concurrency.domain.market.entity.Item;
import CEOS.concurrency.domain.market.repository.ItemRepository;
import CEOS.concurrency.domain.market.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final int INITIAL_QUANTITY = ItemService.INITIAL_QUANTITY;

    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public void run(String... args) {
        itemRepository.findById(1L).ifPresentOrElse(
                item -> item.resetStock(INITIAL_QUANTITY),
                () -> itemRepository.save(Item.builder()
                        .name("ChatGPT Pro 50% 할인 이용권")
                        .price(10000)
                        .stock(INITIAL_QUANTITY)
                        .build())
        );
    }
}
