# concurrency-control
동시성 제어 학습 프로젝트

---

## 시나리오

**상품 구매 API**에 동시에 여러 요청이 들어올 때 재고가 정확하게 감소하는지 검증한다.

- 상품 초기 재고: 1000개
- 테스트: 10명의 유저가 각 50번씩 총 500번 구매 요청

---

## 왜 동시성 문제가 발생하는가

### 현재 구매 로직

```java
@Transactional
public PurchaseResponse purchase(UUID memberUuid) {
    Item item = itemRepository.findById(ITEM_ID); // (1) 재고 조회

    if (item.getQuantity() <= 0) {               // (2) 재고 확인
        throw new BusinessException(OUT_OF_STOCK);
    }

    purchaseLogRepository.save(...);             // (3) 구매 기록
    item.decreaseQuantity();                     // (4) 재고 감소
}
```

### Lost Update

`(1) 조회 → (4) 감소` 사이에 다른 트랜잭션이 끼어드는 것이 문제다.

```
시각  TX A                      TX B
───────────────────────────────────────────
t1    SELECT quantity → 100
t2                          SELECT quantity → 100  ← 같은 값을 읽음
t3    quantity > 0 통과
t4                          quantity > 0 통과
t5    UPDATE quantity = 99
t6                          UPDATE quantity = 99   ← A의 감소를 덮어씀
t7    COMMIT
t8                          COMMIT

결과: 2번 구매됐지만 재고는 1만 감소 (99개)
```

여러 트랜잭션이 동시에 같은 값을 읽고 각자 1씩 감소시키면, 마지막으로 커밋한 트랜잭션의 값만 남아 중간 감소들이 사라진다. 이를 **Lost Update(갱신 손실)** 라고 한다.

### Deadlock

동시에 같은 row를 UPDATE하려 할 때 MySQL InnoDB의 lock 경합으로 Deadlock이 발생한다.

`purchase_log`가 `item`을 FK로 참조하기 때문에, INSERT 시 MySQL이 item row에 **Shared Lock**을 걸게 된다. 이후 `UPDATE item`에서 **Exclusive Lock**으로 업그레이드 시도할 때, 서로 상대방의 Shared Lock을 기다리는 상황이 만들어진다.

```
TX A: INSERT purchase_log → item에 Shared Lock 획득
TX B: INSERT purchase_log → item에 Shared Lock 획득

TX A: UPDATE item → Exclusive Lock 요청 → TX B 대기
TX B: UPDATE item → Exclusive Lock 요청 → TX A 대기

→ 교착 상태 → MySQL이 하나를 강제 rollback → 500 에러
```

---

## 실험 결과

### JUnit + CountDownLatch (단위 테스트)

100개 스레드가 동시에 `ItemService.purchase()`를 직접 호출한 결과.

| 항목 | 값 |
|---|---|
| 초기 재고 | 100개 |
| 동시 요청 수 | 100건 |
| 구매 성공 건수 | 100건 |
| 최종 잔여 재고 | **87개** (0이어야 함) |
| 누락된 감소 | **87건** |

→ 100건 모두 성공했지만 재고는 13개만 감소. **Lost Update 재현 성공.**

### k6 (HTTP 부하 테스트)

실제 HTTP 요청으로 10명의 유저가 50번씩 구매 API를 호출한 결과.

| 항목 | 값 |
|---|---|
| 초기 재고 | 1000개 |
| 총 요청 수 | 500건 |
| 구매 성공 (HTTP 200) | 149건 |
| Deadlock으로 실패 (HTTP 500) | 351건 |
| 최종 잔여 재고 | 936개 |
| 실제 재고 감소 | 64개 |
| 누락된 감소 (Lost Update) | **85건** |

→ 500건 중 149건만 성공했고, 그 중에서도 85건의 재고 감소가 유실됨.

#### 사용자별 구매 성공 건수 (예시)

| 유저 | 요청 성공 |
|---|---|
| k6u0 | 8건 |
| k6u1 | 18건 |
| k6u2 | 20건 |
| k6u3 | 20건 |
| k6u4 | 21건 |
| k6u5 | 11건 |
| k6u6 | 8건 |
| k6u7 | 14건 |
| k6u8 | 12건 |
| k6u9 | 17건 |

---

## 발생한 동시성 문제 요약

| 문제 | 원인 | 증상 |
|---|---|---|
| **Lost Update** | 동시 트랜잭션이 같은 재고값을 읽고 각자 감소 | 성공한 요청 수보다 재고 감소가 적음 |
| **Deadlock** | FK로 인한 Shared Lock + UPDATE 시 Exclusive Lock 경합 | 트랜잭션 강제 rollback → HTTP 500 |

두 문제 모두 **조회 시점에 아무런 lock을 걸지 않기 때문**에 발생한다.
`SELECT FOR UPDATE`(Pessimistic Lock) 등의 동시성 제어를 적용하면 조회 시점부터 Exclusive Lock을 획득하여 두 문제를 동시에 해결할 수 있다.
