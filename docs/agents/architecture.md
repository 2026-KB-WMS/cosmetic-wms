# 아키텍처

헥사고날 아키텍처 + DDD. 단일 Gradle 모듈, 단일 Spring Boot 애플리케이션이다.

## 패키지 구조

루트 패키지는 `com.kb.cosmetic_wms`이다.

```
global/                       # 전역 설정과 공통 모듈
├── common/                   # BaseEntity(JPA Auditing), AuditorAwareImpl
├── config/                   # SecurityConfig, JpaConfig, AsyncConfig
├── error/                    # ErrorCode, BusinessException, GlobalExceptionHandler, ErrorResponseDto
├── event/                    # EventPublisher(포트), SpringEventPublisher
└── geocoding/                # Kakao 지오코딩 어댑터

{domain}/
├── adapter/
│   ├── in/
│   │   ├── web/              # REST Controller, Request/Response DTO
│   │   ├── event/            # 다른 도메인 이벤트를 수신하는 어댑터
│   │   └── bootstrap/        # 기동 시 실행되는 어댑터 (ApplicationRunner)
│   └── out/
│       ├── persistence/      # JPA Entity, JpaRepository, PersistenceAdapter
│       └── external/         # 다른 도메인/외부 시스템 호출 어댑터
├── application/
│   ├── port/in/              # UseCase 인터페이스, Command, Result
│   ├── port/out/             # Port 인터페이스
│   └── service/              # UseCase 구현체
└── domain/
    ├── model/                # 도메인 모델 (순수 POJO)
    ├── enums/
    ├── exception/            # 도메인 예외와 ErrorCode 구현
    ├── valueobject/
    ├── event/                # 이 도메인이 발행하는 이벤트
    └── service/              # 도메인 서비스
```

도메인: `member`, `partner`, `store`, `storage`(Warehouse·Section), `product`(category·product·producttype 하위 구성), `lot`, `inventory`, `inbound`, `inspection`, `outbound`, `order`, `oms`.

도메인에 따라 일부 하위 패키지가 없을 수 있다. 필요할 때만 만든다.

## 의존 규칙

| 계층 | import 가능 | import 금지 |
|---|---|---|
| `domain` | 같은 도메인의 `domain`, JDK, Lombok | Spring, JPA(`jakarta.persistence`), `application`, `adapter` |
| `application` | 같은 도메인의 `domain`·`application`, `global` | 같은 도메인의 `adapter`, 다른 도메인의 `domain`·`adapter` |
| `adapter` | 같은 도메인의 `application`·`domain`, 아래 "도메인 간 연결"의 예외 | 다른 도메인의 `adapter` |

현재 코드는 `domain`의 Spring/JPA import 0건, `application`의 `adapter` import 0건이다. 이 상태를 유지한다.

## 도메인 간 연결

허용되는 방식은 두 가지이며, 둘 다 `adapter` 계층에서만 일어난다.

1. **동기 호출**: `adapter/out/...`이 상대 도메인의 `application.port.in`(UseCase) 또는 `application.port.out`을 구현·호출한다. 예: `outbound.adapter.out` → `inventory.application.port`.
2. **이벤트 구독**: `adapter/in/event`가 상대 도메인의 `domain.event`를 구독한다. 예: `inspection.adapter.in.event` → `inbound.domain.event.InboundCompletedEvent`.

다른 도메인은 **ID로 참조**한다 (`Long lotId`, `Long productId`). 다른 도메인의 도메인 모델/엔티티를 필드로 갖지 않는다. 한 도메인이 다른 도메인의 `domain.model`을 import해야 한다면 경계가 잘못된 것이다.

알려진 예외(정리 대상): `storage.adapter.out`이 `product`의 `domain` 패키지를 직접 import한다. 새 코드에서 따라 하지 않는다.

## 포트와 어댑터

- UseCase 인터페이스는 `application/port/in`, 외부 의존(영속성·타 도메인·외부 API)은 `application/port/out`에 정의한다.
- `port/out`에는 Spring Data 타입(`Page`, `Pageable`)을 노출하지 않는다.
- PersistenceAdapter는 JpaRepository를 감싸 포트를 구현하고 Entity ↔ 도메인 모델을 변환한다.
- 도메인 정책이 외부 호출을 필요로 하면, 정책이 포트에 의존하지 않도록 함수형 인터페이스를 도메인에 정의하고 서비스가 주입한다 (예: `DrivingDistanceProvider`, [ADR-0004](../adr/0004-warehouse-assignment-policy.md)).

## 계층별 책임

- **Controller**: 요청 검증과 변환, UseCase 호출, 응답 변환만 한다. 비즈니스 로직 금지.
- **Application service**: 트랜잭션 경계, 유스케이스 흐름 조율, 포트 호출. 불변식은 도메인 모델에 위임한다.
- **Domain model**: 불변식과 상태 전이를 메서드로 캡슐화한다. 외부에서 상태를 setter로 바꾸지 않는다.
- **Persistence adapter**: 변환과 쿼리만 한다. 비즈니스 규칙 금지.

## 보안

`SecurityConfig`는 `/api/v1/**`, Swagger, `/docs/**`를 `permitAll`로 두고 `httpBasic`·`formLogin`·CSRF를 끈 상태다. 인증/인가 체계는 아직 없다. 인증을 전제로 한 코드를 가정하지 않는다 (이슈 #126 참고).
