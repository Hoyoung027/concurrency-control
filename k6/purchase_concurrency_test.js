/**
 * 동시성 문제 검증 테스트 - k6
 *
 * [설치]
 *   Mac  : brew install k6
 *   Linux: sudo snap install k6
 *   기타  : https://grafana.com/docs/k6/latest/set-up/install-k6/
 *
 * [실행] 프로젝트 루트에서:
 *   k6 run k6/purchase_concurrency_test.js
 *
 * [전제 조건]
 *   - 서버가 localhost:8080 에서 실행 중이어야 함
 *   - DataInitializer의 INITIAL_QUANTITY와 아래 값을 맞춰야 함
 */

import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = 'http://localhost:8080';
const USERS_COUNT = 10;
const REQUESTS_PER_USER = 50;
const INITIAL_QUANTITY = 1000; // DataInitializer.INITIAL_QUANTITY와 동일하게 맞출 것
const CHARACTER_TYPES = ['CAT', 'DOG', 'RABBIT', 'DEER', 'LION', 'FOX', 'BEAR', 'PENGUIN', 'HAMSTER', 'FROG'];

export const options = {
  vus: USERS_COUNT,                            // 동시 사용자 10명
  iterations: USERS_COUNT * REQUESTS_PER_USER, // 총 500번 (VU당 50번)
};

// 테스트 시작 전 1회 실행 - 유저 생성 & 로그인해서 토큰 수집
export function setup() {
  const tokens = [];

  const suffix = String(Date.now()).slice(-6); // 타임스탬프 끝 6자리로 중복 방지
  for (let i = 0; i < USERS_COUNT; i++) {
    const nickname = `k6u${i}_${suffix}`; // 최대 12자 (20자 제한 이내)
    const password = 'password123';
    const characterType = CHARACTER_TYPES[i % CHARACTER_TYPES.length];

    const signupRes = http.post(
      `${BASE_URL}/auth/signup`,
      JSON.stringify({ nickname, password, characterType }),
      { headers: { 'Content-Type': 'application/json' } }
    );

    if (signupRes.status !== 200) {
      console.error(`signup 실패 [${i}]: ${signupRes.status} ${signupRes.body}`);
      continue;
    }

    const loginRes = http.post(
      `${BASE_URL}/auth/login`,
      JSON.stringify({ nickname, password }),
      { headers: { 'Content-Type': 'application/json' } }
    );

    if (loginRes.status !== 200) {
      console.error(`login 실패 [${i}]: ${loginRes.status} ${loginRes.body}`);
      continue;
    }

    const accessToken = loginRes.json('payload.accessToken');
    tokens.push(accessToken);
  }

  console.log(`준비 완료: ${tokens.length}명 로그인`);
  return { tokens };
}

// 각 VU가 50번씩 구매 요청
export default function (data) {
  const token = data.tokens[(__VU - 1) % data.tokens.length];

  const res = http.post(
    `${BASE_URL}/market/item/purchase`,
    null,
    { headers: { Authorization: `Bearer ${token}` } }
  );

  const success = check(res, {
    '구매 성공 (200)': (r) => r.status === 200,
    '재고 부족 (409)': (r) => r.status === 409,
  });

  // 200도 409도 아닌 예상 밖 응답은 출력
  if (res.status !== 200 && res.status !== 409) {
    console.error(`예상 밖 응답 [VU ${__VU}]: status=${res.status} body=${res.body}`);
  }
}

// 테스트 종료 후 1회 실행 - 결과 요약
export function teardown(data) {
  const res = http.get(`${BASE_URL}/market/item`, {
    headers: { Authorization: `Bearer ${data.tokens[0]}` },
  });
  if (res.status !== 200) {
    console.error(`상품 조회 실패: status=${res.status} body=${res.body}`);
    return;
  }

  const item = res.json('payload');
  const decreased = INITIAL_QUANTITY - item.quantity;
  const lostUpdates = item.purchaseAttempts - decreased;
  const totalRequested = USERS_COUNT * REQUESTS_PER_USER;

  console.log('\n========================================');
  console.log('            동시성 테스트 결과            ');
  console.log('========================================');
  console.log(`총 요청 수      : ${totalRequested}건`);
  console.log(`구매 성공 건수  : ${item.purchaseAttempts}건`);
  console.log(`재고 초기값     : ${INITIAL_QUANTITY}개`);
  console.log(`잔여 재고       : ${item.quantity}개`);
  console.log(`실제 감소 수    : ${decreased}개`);
  console.log(`누락된 감소     : ${lostUpdates}건  ← Lost Update`);
  console.log('----------------------------------------');
  console.log('사용자별 구매 성공 건수:');

  const byMember = item.purchaseAttemptsByMember;
  Object.keys(byMember).sort().forEach((nickname) => {
    console.log(`  ${nickname}: ${byMember[nickname]}건`);
  });

  console.log('========================================');

  if (lostUpdates > 0) {
    console.log(`⚠️  동시성 문제 발생! ${lostUpdates}건의 재고 감소가 유실됨`);
  } else {
    console.log('✅ 동시성 문제 없음');
  }
}
