# 구매 동시성 부하 테스트

프로젝트 루트에서 실행합니다. macOS에서 k6가 없다면 `brew install k6`로 설치합니다.
백엔드와 DB가 실행 중이어야 합니다. 기본값은 가상 사용자 10명, 사용자당 구매 50회(총 500회), 상품 ID 1입니다.

```sh
k6 run k6/purchase_concurrency_test.js
```

원격 서버는 `BASE_URL`로 지정합니다.

```sh
k6 run \
  -e BASE_URL=https://2fe4-2001-e60-1259-7a05-ec1c-b851-c005-a7e9.ngrok-free.app \
  k6/purchase_concurrency_test.js
```

작은 규모로 연결을 확인하려면:

```sh
k6 run -e BASE_URL=http://localhost:8080 \
  -e USERS_COUNT=2 -e REQUESTS_PER_USER=2 \
  k6/purchase_concurrency_test.js
```

부하를 변경하려면:

```sh
k6 run -e BASE_URL=http://localhost:8080 \
  -e ITEM_ID=1 -e USERS_COUNT=100 -e REQUESTS_PER_USER=10 \
  -e MAX_DURATION=10m k6/purchase_concurrency_test.js
```

## 동작과 데이터

- 매 실행마다 가상 사용자 수만큼 실제 회원을 가입시키고 로그인합니다. 회원과 주문은 테스트 후에도 남습니다.
- 기존 주문과 재고를 삭제하거나 초기화하지 않습니다. 시작 재고가 0이면 중단합니다.
- 상품 조회와 구매에 `itemId`를 전달하고, 재고는 `payload.stock`, 주문 수는 `/market/stats`의 `payload.purchaseAttempts`를 사용합니다.
- 테스트 전후 값을 비교하므로 초기 재고가 반드시 1,000개일 필요는 없습니다.
- `/market/stats`는 모든 상품의 주문 수를 합산합니다. 정확한 비교를 위해 테스트 중 다른 구매·초기화·서버 재시작이 없어야 합니다.
- 현재 `/market/item/reset`은 모든 주문·상품을 삭제하고 상품 1을 재생성하므로 스크립트에서 자동 호출하지 않습니다.
- `per-vu-iterations`로 사용자마다 지정한 횟수를 실행합니다. 요청을 정확히 같은 순간에 출발시키는 방식은 아닙니다.
- 기본 구매 실행 제한은 10분, 개별 HTTP 요청 제한은 30초입니다.

## 결과 해석

- `purchase_requests`: 실제 완료된 구매 HTTP 요청 수. 설정한 총 요청 수와 같아야 합니다.
- `purchase_success`: HTTP 200 구매 응답 수.
- `purchase_out_of_stock`: HTTP 409 재고 부족 응답 수. 재고보다 요청이 많으면 정상적으로 발생할 수 있습니다.
- 시작 재고가 1,000개이고 500회 구매하면 기대 주문 증가량은 500건, 기대 잔여 재고는 500개입니다.
- 주문 증가량과 재고 감소량이 다르거나, 기대 주문 수·잔여 재고가 다르거나, 예상 밖 HTTP 오류가 발생하면 threshold 실패로 종료 코드가 0이 아니게 됩니다.
- `http_req_duration`은 응답 시간이며 가입·로그인·조회도 전체 HTTP 지표에 포함됩니다. 구매 수는 `purchase_*` 지표로 확인합니다.
- 재고 부족도 허용하므로 `http_req_failed=0`만으로 구매가 모두 성공했다는 뜻은 아닙니다. 최종 재고 체크와 구매 지표를 함께 확인하세요.

현재 서비스의 `synchronized`는 조회부터 커밋까지 보호하지 않으므로 재고 일치 검증에 실패할 수 있습니다.
테스트가 통과하더라도 해당 실행에서 문제가 관측되지 않았다는 의미이며, 모든 실행의 동시성 안전성을 증명하지는 않습니다.

참고: [k6 per-vu-iterations](https://grafana.com/docs/k6/latest/using-k6/scenarios/executors/per-vu-iterations/)
