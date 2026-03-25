# CEOS 결제 서버

PortOne 결제 API를 참고하여 구성한 결제 서버입니다.

[PortOne REST API v2 명세서](https://developers.portone.io/api/rest-v2/payment?v=v2#get%20%2Fpayments%2F%7BpaymentId%7D)

---

## Payment API 명세서

### 공통 사항

결제는 항상 GitHub ID로 식별되는 가맹점 단위로 관리됩니다. 각 가맹점은 API Secret Key를 발급받아야 결제 API를 사용할 수 있습니다.

| 항목 | 내용 |
|------|------|
| Base URL | `https://diggindie.com/ceos/` |
| 인증 방식 | `Authorization: Bearer {API_SECRET_KEY}` |
| Content-Type | `application/json` |

---

### 1. 가맹점 API Secret 조회 (인증키 발급)

**`GET /auth/{githubId}`**

가맹점의 API Secret Key를 조회합니다. 존재하지 않으면 신규 생성합니다.

- **인증 불필요**

**Path Parameter**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `githubId` | String | Y | 가맹점의 GitHub ID |

**Response `200 OK`**

```json
{
  "code": 200,
  "message": "결제용 API Secret 조회",
  "data": {
    "githubId": "Hoyoung027",
    "apiSecretKey": "eyJhbGci...",
    "createdAt": "2026-02-07T02:07:00"
  }
}
```

---

### 2. 즉시 결제 요청

**`POST /payments/{paymentId}/instant`**

결제를 즉시 처리합니다. 10% 확률로 랜덤 실패가 발생합니다.

- **인증 필요** (`Authorization: Bearer {API_SECRET_KEY}`)

**Path Parameter**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `paymentId` | String | Y | 가맹점이 생성한 고유 결제 ID (ex. `20251022_0001`) |

**Request Body**

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `storeId` | String | Y | 가맹점 ID — GitHub ID (ex. `Hoyoung027`) |
| `orderName` | String | Y | 주문명 |
| `totalPayAmount` | Integer | Y | 결제 금액 (0 초과) |
| `currency` | String | Y | 통화 (`KRW` \| `USD`) |
| `customData` | String | N | 가맹점 커스텀 데이터 |

**Request Body 예시**

```json
{
  "storeId": "Hoyoung027",
  "orderName": "노트북 구매",
  "totalPayAmount": 1500000,
  "currency": "KRW",
  "customData": "{\"item\":\"노트북\",\"quantity\":1}"
}
```

**Response `200 OK`**

```json
{
  "code": 200,
  "message": "결제 처리 완료",
  "data": {
    "paymentId": "20251022_0001",
    "paymentStatus": "PAID",
    "orderName": "노트북 구매",
    "pgProvider": "string",
    "currency": "KRW",
    "customData": "{\"item\":\"노트북\",\"quantity\":1}",
    "paidAt": "2026-02-07T02:07:00"
  }
}
```

**Error Cases**

| HTTP Status | 에러 코드 | 설명 |
|-------------|-----------|------|
| 403 | `STORE_ID_MISMATCH` | 토큰의 가맹점과 요청의 `storeId` 불일치 |
| 404 | `STORE_NOT_FOUND` | 존재하지 않는 가맹점 |
| 409 | `DUPLICATE_PAYMENT_ID` | 이미 존재하는 `paymentId` |
| 500 | `PAYMENT_FAILED` | 랜덤 실패 정책에 의한 결제 실패 (10% 확률) |

---

### 3. 결제 취소

**`POST /payments/{paymentId}/cancel`**

`PAID` 상태인 결제를 취소합니다.

- **인증 필요** (`Authorization: Bearer {API_SECRET_KEY}`)

**Path Parameter**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `paymentId` | String | Y | 취소할 결제 ID |

**Response `200 OK`**

```json
{
  "code": 200,
  "message": "결제 취소 완료",
  "data": {
    "paymentId": "20251022_0001",
    "paymentStatus": "CANCELLED",
    "orderName": "노트북 구매",
    "pgProvider": "string",
    "currency": "KRW",
    "customData": "{\"item\":\"노트북\",\"quantity\":1}",
    "paidAt": "2026-02-07T02:07:00"
  }
}
```

**Error Cases**

| HTTP Status | 에러 코드 | 설명 |
|-------------|-----------|------|
| 404 | `STORE_NOT_FOUND` | 존재하지 않는 가맹점 |
| 404 | `PAYMENT_NOT_FOUND` | 존재하지 않는 결제 내역 |
| 409 | `PAYMENT_NOT_CANCELLABLE` | `PAID` 상태가 아닌 결제는 취소 불가 |

---

### 4. 결제 내역 조회

**`GET /payments/{paymentId}`**

결제 내역을 단건 조회합니다.

- **인증 필요** (`Authorization: Bearer {API_SECRET_KEY}`)

**Path Parameter**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `paymentId` | String | Y | 조회할 결제 ID |

**Response `200 OK`**

```json
{
  "code": 200,
  "message": "결제 내역 조회",
  "data": {
    "paymentId": "20251022_0001",
    "paymentStatus": "PAID | FAILED | CANCELLED",
    "orderName": "노트북 구매",
    "pgProvider": "string",
    "currency": "KRW | USD",
    "customData": "{\"item\":\"노트북\",\"quantity\":1}",
    "paidAt": "2026-02-07T02:07:00"
  }
}
```

**Error Cases**

| HTTP Status | 에러 코드 | 설명 |
|-------------|-----------|------|
| 404 | `STORE_NOT_FOUND` | 존재하지 않는 가맹점 |
| 404 | `PAYMENT_NOT_FOUND` | 존재하지 않는 결제 내역 |


## 참고 사항
1) paymentId는 본인 상점마다 고유한 번호를 부여하여 사용합니다. 
  - 예) "20251022_0001", "20251022_0002" ...
  - PortOne에서도 이러한 방법을 사용하고 있습니다.
2) storeId는 가맹점의 고유한 식별자로 GitHub ID를 사용해주세요.
3) orderName은 주문명으로 고객에게 표시되는 정보입니다.
4) 일반적으로 custom data에 상품 정보, 수량등 주문 정보를 담아 사용합니다. (필수X)
- 예) ```{"item":"노트북","quantity":1}```
5) 모든 결제 내역 조회는 존재하지 않습니다.
6) 개인 서버에서 실행중이므로 과도한 요청에 의해 서버가 다운될 수 있습니다. 문제가 생겼다면 슬랙으로 연락주세요!

