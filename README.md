# concurrency-control
동시성 제어 학습 프로젝트

---

## 인증 / 인가 구조

### 회원가입 & 로그인

| 엔드포인트 | 설명 |
|---|---|
| `POST /auth/signup` | 회원가입. 닉네임 중복 없으면 비밀번호 BCrypt 인코딩 후 저장 |
| `POST /auth/login` | 로그인. `AuthenticationManager`로 자격증명 검증 후 Access/Refresh Token 발급 |

**로그인 흐름**
```
POST /auth/login
  └─ MemberService.login()
       └─ AuthenticationManager.authenticate()
            └─ CustomUserDetailsService.loadUserByUsername()  ← 닉네임으로 Member 조회
       └─ JwtProvider.generateAccessToken(uuid)
       └─ JwtProvider.generateRefreshToken(uuid)
  └─ LoginResponse(accessToken, refreshToken) 반환
```

---

### 인증 (Authentication) 흐름

모든 요청은 `JwtFilter`를 통과합니다.

```
Request
  └─ JwtFilter
       ├─ Authorization 헤더 없음 → SecurityContext 비운 채로 통과
       ├─ 토큰 유효 → SecurityContext에 인증 정보 설정 후 통과
       └─ 토큰 무효 → request attribute에 예외 저장, SecurityContext 비운 채로 통과
            └─ (이후 인증 필요한 경로 접근 시 → JwtAuthenticationEntryPoint에서 예외 꺼내 반환)
```

---

### 인가 (Authorization) 전략

인가는 두 레이어를 혼용합니다.

**1. SecurityConfig — URL 레벨 (공개 여부 구분)**

```java
.requestMatchers("/auth/signup", "/auth/login").permitAll()
.anyRequest().authenticated()
```

인증이 필요 없는 공개 API는 `permitAll()`로 명시합니다.

**2. `@PreAuthorize` — 메서드 레벨 (권한 구분)**

```java
@PreAuthorize("hasRole('ADMIN')")
```

인증된 사용자 중 특정 권한(`ADMIN` 등)이 필요한 API는 `@PreAuthorize`로 제어합니다.

**결론:** `permitAll()`로 공개/비공개를 나누고, 세밀한 권한 제어는 `@PreAuthorize`에서 담당합니다. `@PreAuthorize`만으로 인증 여부까지 제어하는 방식은 어노테이션 누락 시 보안 허점이 생길 수 있어 채택하지 않았습니다.

---

### 에러 핸들링

에러 발생 위치에 따라 처리 주체가 다릅니다.

```
┌─────────────────────────────────────────────────────────┐
│ JwtFilter (Servlet Filter 계층)                          │
│  └─ 토큰 무효 → request attribute에 BusinessException 저장│
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│ Spring Security 예외 처리                                 │
│  ├─ 미인증 접근 → JwtAuthenticationEntryPoint → 401      │
│  │    └─ request attribute에 토큰 예외 있으면 함께 반환   │
│  └─ 권한 부족  → JwtAccessDeniedHandler → 403            │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│ GlobalExceptionHandler (@RestControllerAdvice)           │
│  ├─ BusinessException  → 비즈니스 에러 코드 반환          │
│  ├─ @PreAuthorize 실패 → 401 / 403                       │
│  └─ Validation 실패    → 400                             │
└─────────────────────────────────────────────────────────┘
```

#### 토큰 에러 처리 상세

`JwtFilter`는 토큰 검증 실패 시 예외를 바로 던지지 않고, `request attribute`에 저장한 뒤 SecurityContext만 비웁니다. 이후 인증이 필요한 경로에 접근하면 Spring Security가 `JwtAuthenticationEntryPoint`를 호출하고, 이 시점에 attribute에서 예외를 꺼내 구체적인 토큰 에러를 반환합니다.

```java
// JwtFilter — 토큰 검증 실패 시
request.setAttribute("JWT_EXCEPTION", e);
SecurityContextHolder.clearContext();
// 예외를 던지지 않고 필터 체인 계속 진행

// JwtAuthenticationEntryPoint — 401 발생 시
BusinessException jwtEx = (BusinessException) request.getAttribute("JWT_EXCEPTION");
if (jwtEx != null) {
    // TOKEN_EXPIRED / TOKEN_MALFORMED / TOKEN_INVALID 반환
} else {
    // 일반 UNAUTHORIZED 반환
}
```

#### JWT 에러 코드

| 에러 코드 | HTTP | 원인 |
|---|---|---|
| `TOKEN_EXPIRED` | 401 | 토큰 만료 |
| `TOKEN_MALFORMED` | 401 | 형식 오류 또는 서명 불일치 |
| `TOKEN_INVALID` | 401 | 그 외 유효하지 않은 토큰 |

#### 공통 에러 응답 형식

```json
{
  "status": 401,
  "description": "만료된 토큰입니다.",
  "payload": null
}
```
