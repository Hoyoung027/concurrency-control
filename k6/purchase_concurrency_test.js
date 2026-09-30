/** 실행 방법과 결과 해석: k6/README.md */
import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

function positiveInteger(name, fallback) {
  const value = Number(__ENV[name] || fallback);
  if (!Number.isSafeInteger(value) || value < 1) {
    throw new Error(`${name}은 양의 정수여야 합니다.`);
  }
  return value;
}

const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');
const USERS_COUNT = positiveInteger('USERS_COUNT', 10);
const REQUESTS_PER_USER = positiveInteger('REQUESTS_PER_USER', 50);
const ITEM_ID = positiveInteger('ITEM_ID', 1);
const TOTAL_REQUESTS = USERS_COUNT * REQUESTS_PER_USER;
const CHARACTER_TYPES = ['CAT', 'DOG', 'RABBIT', 'DEER', 'LION', 'FOX', 'BEAR', 'PENGUIN', 'HAMSTER', 'FROG'];
const purchaseRequests = new Counter('purchase_requests');
const purchaseSuccess = new Counter('purchase_success');
const purchaseOutOfStock = new Counter('purchase_out_of_stock');

export const options = {
  scenarios: {
    purchase: {
      executor: 'per-vu-iterations',
      vus: USERS_COUNT,
      iterations: REQUESTS_PER_USER,
      maxDuration: __ENV.MAX_DURATION || '10m',
    },
  },
  setupTimeout: '5m',
  thresholds: {
    checks: ['rate==1'],
    http_req_failed: ['rate==0'],
    purchase_requests: [`count==${TOTAL_REQUESTS}`],
  },
};

function params(token, name) {
  return {
    headers: {
      'Content-Type': 'application/json',
      'ngrok-skip-browser-warning': 'true',
      ...(token ? { Cookie: `access_token=${token}` } : {}),
    },
    tags: { name },
    timeout: '30s',
    redirects: 0,
  };
}

function requireOk(res, label) {
  if (!check(res, { [label]: (r) => r.status === 200 })) {
    throw new Error(`${label}: HTTP ${res.status}`);
  }
}

function snapshot(token) {
  const itemRes = http.get(`${BASE_URL}/market/item?itemId=${ITEM_ID}`, params(token, 'get_item'));
  requireOk(itemRes, '상품 조회 성공');
  const statRes = http.get(`${BASE_URL}/market/stats`, params(token, 'get_stats'));
  requireOk(statRes, '통계 조회 성공');
  const item = itemRes.json('payload');
  const stats = statRes.json('payload');
  if (!item || !Number.isSafeInteger(item.stock) || item.stock < 0 ||
      !stats || !Number.isSafeInteger(stats.purchaseAttempts) || stats.purchaseAttempts < 0) {
    check(false, { '조회 응답 형식 정상': (valid) => valid });
    throw new Error('stock 또는 purchaseAttempts 응답이 올바르지 않습니다.');
  }
  return { stock: item.stock, orders: stats.purchaseAttempts };
}

export function setup() {
  const tokens = [];
  const suffix = `${Date.now().toString(36)}${Math.random().toString(36).slice(2, 6)}`;
  for (let i = 0; i < USERS_COUNT; i++) {
    const nickname = `k6${i.toString(36)}_${suffix}`;
    const password = 'password123';
    // 가입/로그인 때 이전 사용자의 쿠키가 섞이지 않도록 초기화합니다.
    http.cookieJar().clear(BASE_URL);
    const signupRes = http.post(`${BASE_URL}/auth/signup`, JSON.stringify({
      nickname, password, characterType: CHARACTER_TYPES[i % CHARACTER_TYPES.length],
    }), params(null, 'signup'));
    requireOk(signupRes, '회원 가입 성공');
    const loginRes = http.post(`${BASE_URL}/auth/login`, JSON.stringify({ nickname, password }), params(null, 'login'));
    requireOk(loginRes, '로그인 성공');
    const cookies = loginRes.cookies.access_token;
    const token = cookies && cookies[0] && cookies[0].value;
    if (!token) throw new Error(`사용자 ${i}: access_token 쿠키가 없습니다.`);
    tokens.push(token);
  }
  http.cookieJar().clear(BASE_URL);
  const before = snapshot(tokens[0]);
  if (before.stock === 0) throw new Error('재고가 0입니다. 테스트할 재고를 준비한 뒤 다시 실행하세요.');
  console.log(`준비 완료: ${BASE_URL}, 상품 ${ITEM_ID}, ${USERS_COUNT}명 × ${REQUESTS_PER_USER}회, 시작 재고 ${before.stock}`);
  return { tokens, before };
}

export default function (data) {
  const token = data.tokens[__VU - 1];
  const requestParams = params(token, 'purchase');
  requestParams.responseCallback = http.expectedStatuses(200, 409);
  const res = http.post(`${BASE_URL}/market/item/purchase?itemId=${ITEM_ID}`, null, requestParams);
  purchaseRequests.add(1);
  purchaseSuccess.add(res.status === 200 ? 1 : 0);
  purchaseOutOfStock.add(res.status === 409 ? 1 : 0);
  if (!check(res, { '구매 응답 정상 (200 또는 409)': (r) => r.status === 200 || r.status === 409 })) {
    console.error(`예상 밖 구매 응답: VU=${__VU}, HTTP ${res.status}`);
  }
}

export function teardown(data) {
  const after = snapshot(data.tokens[0]);
  const ordersCreated = after.orders - data.before.orders;
  const decreased = data.before.stock - after.stock;
  const difference = ordersCreated - decreased;
  const expectedOrders = Math.min(data.before.stock, TOTAL_REQUESTS);

  console.log(`설정한 구매 요청: ${TOTAL_REQUESTS}건`);
  console.log(`생성된 주문: ${ordersCreated}건 (기대: ${expectedOrders}건)`);
  console.log(`재고: ${data.before.stock} → ${after.stock}, 실제 감소: ${decreased}개`);
  console.log(`주문 수 - 재고 감소량: ${difference} (양수이면 재고 감소 유실 또는 초과 판매 가능)`);

  check({ ordersCreated, decreased, after }, {
    '주문 수와 재고 감소량 일치': (r) => r.ordersCreated === r.decreased,
    '기대 주문 수 일치': (r) => r.ordersCreated === expectedOrders,
    '기대 잔여 재고 일치': (r) => r.after.stock === data.before.stock - expectedOrders,
  });
}
