package CEOS.concurrency.domain.market.service;

import CEOS.concurrency.common.enums.CharacterType;
import CEOS.concurrency.domain.market.entity.Item;
import CEOS.concurrency.domain.market.repository.ItemRepository;
import CEOS.concurrency.domain.market.repository.OrderRepository;
import CEOS.concurrency.domain.member.entity.Member;
import CEOS.concurrency.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemServiceConcurrencyTest {

    private static final int THREAD_COUNT = 100;

    @Autowired private ItemService itemService;
    @Autowired private ItemRepository itemRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private final List<UUID> memberUuids = new ArrayList<>();

    @BeforeAll
    void setUpMembers() {
        CharacterType[] types = CharacterType.values();
        for (int i = 0; i < THREAD_COUNT; i++) {
            Member member = Member.builder()
                    .nickname("testuser" + i)
                    .password("password")
                    .characterType(types[i % types.length])
                    .build();
            memberUuids.add(memberRepository.save(member).getUuid());
        }
    }

    @BeforeEach
    void resetItem() {
        orderRepository.deleteAll();
        jdbcTemplate.update("UPDATE item SET stock = 100 WHERE id = 1");
    }

    @Test
    void 동시성_제어_없이_100명이_동시에_구매하면_재고가_부정확하게_감소한다() throws InterruptedException {
        // given
        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch startLatch = new CountDownLatch(1);   // 동시 출발 신호
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT); // 완료 대기

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final UUID uuid = memberUuids.get(i);
            executor.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 준비될 때까지 대기
                    itemService.purchase(ItemService.ITEM_ID, uuid);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // when
        startLatch.countDown(); // 100개 스레드 동시 출발
        doneLatch.await();      // 모두 완료될 때까지 대기
        executor.shutdown();

        // then
        Item item = itemRepository.findById(ItemService.ITEM_ID).orElseThrow();
        int remainingStock = item.getStock();

        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("구매 성공: " + successCount.get() + "건");
        System.out.println("구매 실패: " + failCount.get() + "건");
        System.out.println("최종 재고: " + remainingStock + "개");
        System.out.println("기대 재고: 0개 (100개 재고, 100명 구매)");
        System.out.println("재고 손실: " + remainingStock + "개 (감소됐어야 하는데 누락된 횟수)");

        // 동시성 문제가 없다면 성공 건수 + 잔여 재고 = 100 이어야 함
        // 동시성 문제가 있다면 잔여 재고가 0보다 크게 남음 (Lost Update)
        assertThat(remainingStock).isEqualTo(0);
    }
}
