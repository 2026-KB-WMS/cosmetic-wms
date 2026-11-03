# 인증과 인가

결정 배경은 [ADR-0006](../adr/0006-jwt-authentication-and-rbac.md)을 참고한다.

## 인증 흐름

| API | 인증 | 설명 |
|---|---|---|
| `POST /api/v1/auth/login` | 공개 | 로그인 ID/비밀번호 → Access Token + Refresh Token |
| `POST /api/v1/auth/reissue` | 공개 | Refresh Token → 새 Access Token + 새 Refresh Token. 사용한 Refresh Token은 폐기 |
| `POST /api/v1/auth/logout` | 공개 | 제출한 Refresh Token을 폐기 |
| `POST /api/v1/members/signup` | 공개 | 회원 가입 (`ROLE_HEADQUARTERS`는 직접 가입 불가) |

- API 호출은 `Authorization: Bearer <accessToken>` 헤더로 한다.
- Access Token이 만료되면 `401`과 에러 코드 `INVALID_TOKEN`이 온다. 클라이언트는 `/auth/reissue`로 재발급한다.
- Refresh Token이 유효하지 않으면 `401`과 `INVALID_REFRESH_TOKEN`이다. 다시 로그인해야 한다.
- 이미 사용한 Refresh Token을 다시 제출하면 거부된다 (회전).

| 상태 | HTTP | errorCode |
|---|---|---|
| 토큰 없음 | 401 | `UNAUTHENTICATED` |
| 토큰이 만료·위조·형식 오류 | 401 | `INVALID_TOKEN` |
| 역할이 부족함 | 403 | `ACCESS_DENIED` |

## 역할별 접근 제어

`SecurityConfig`가 URL과 메서드로 판정한다. 본사(`ROLE_HEADQUARTERS`)는 모든 API에 접근할 수 있다. **규칙에 없는 경로는 거부한다.**

| 영역 | 본사 | 창고 관리자 | 점주 |
|---|:-:|:-:|:-:|
| 상품·카테고리·상품 유형·가맹점 조회 | O | O | O |
| 상품·카테고리·상품 유형·가맹점 등록·수정·삭제 | O | | |
| 협력사·창고 조회 | O | O | |
| 협력사·창고 등록 | O | | |
| 로트, 입고, 품질검사, 재고, 출고 (조회·변경 모두) | O | O | |
| 발주 생성·확정·취소·수령 확인 | O | | O |
| 발주 준비·출하 | O | O | |
| 운영 관리 (`/api/v1/admin/**`: 실패한 이벤트 조회·재처리·폐기) | O | | |

범위 밖: 담당 창고나 소속 가맹점 단위의 데이터 접근 제어(예: 창고 관리자가 다른 창고의 입고를 조회하는 것), 로그인 시도 횟수 제한.

## 새 엔드포인트를 추가할 때

1. `SecurityConfig`에 역할 규칙을 추가한다. 기존 패턴(`/api/v1/<영역>/**`)에 해당하면 규칙이 이미 적용된다.
2. `AuthorizationMatrixTest`의 `MATRIX`에 한 줄을 추가한다. 이 표가 컨트롤러 매핑 전체와 정확히 일치해야 통과한다. 빠뜨리면 CI가 실패한다.
3. 공개 API는 예외적인 경우에만 추가한다. 인증 없이 호출할 수 있는 이유를 PR에 적는다.

역할 이름(`HEADQUARTERS` 등)은 `Role` enum에서 `ROLE_`을 뗀 값과 같아야 하며 테스트가 이를 검사한다.

## 사용자 정보 읽기와 감사 필드

- `created_by`, `updated_by`와 재고 이력의 행위자(`inventory_transaction.member_id`)는 요청한 로그인 사용자의 회원 ID다 (`AuditorAwareImpl`).
- 인증 정보가 없는 경로(회원 가입, 기동 시 부트스트랩)는 시스템 감사자 `0`(`AuditorAwareImpl.SYSTEM_AUDITOR_ID`)이다.
- `@Async` 실행기는 보안 컨텍스트를 전달한다. 새 비동기 실행기를 만들면 같은 전달 설정을 해야 행위자가 `0`으로 기록되지 않는다.
- `member_id`처럼 `member`를 FK로 참조하는 컬럼에 행위자를 쓰는 작업은 인증된 사용자가 없으면 FK 위반으로 실패한다. 시스템이 스스로 시작하는 작업을 만들 때는 시스템 회원을 정해야 한다.

## 설정

| 환경변수 | 필수 | 기본값 | 설명 |
|---|:-:|---|---|
| `JWT_SECRET` | O | - | 서명 키. 32바이트 이상 |
| `JWT_ACCESS_TOKEN_TTL` | | `30m` | Access Token 유효 시간 |
| `JWT_REFRESH_TOKEN_TTL` | | `14d` | Refresh Token 유효 시간 |
| `REDIS_HOST` | | `localhost` | |
| `REDIS_PORT` | | `6379` | |
| `REDIS_PASSWORD` | | 없음 | |

## 테스트에서

- 컨트롤러 슬라이스 테스트(`@WebMvcTest`)는 API 계약 검증이 목적이라 `@Import(TestSecurityConfig.class)`로 모든 요청을 허용한다. 인가는 이 테스트가 아니라 `AuthorizationMatrixTest`가 운영 `SecurityConfig`로 검증한다.
- 서비스를 직접 호출하는 통합 테스트에서 행위자가 필요하면 `TestAuthentication.authenticateAs(회원ID)`로 로그인한 상태를 만들고 끝나면 `clear()`한다.
- Redis가 필요한 테스트는 `IntegrationTestSupport`를 상속한다 (MySQL과 Redis 컨테이너).
