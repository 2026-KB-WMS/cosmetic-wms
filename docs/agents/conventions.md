# 코드 컨벤션

## 도메인 모델

- 도메인 모델(`domain/model`)은 순수 POJO다. 상태 변경은 의미 있는 도메인 메서드로 노출하고 setter는 두지 않는다.
- 생성 경로는 두 가지로 나눈다.
  - `create(...)`: 새로 만들 때. 검증을 수행한다.
  - `reconstitute(...)`: 저장소에서 복원할 때. 검증을 다시 하지 않는다.
- 값 객체(`domain/valueobject`)는 `record`로 만들고 생성 시 불변식을 검증한다 (`LoginId`, `Email`, `PhoneNumber`).
- 상태 조합이 의미를 가지면 조합 자체를 검증하는 타입으로 만든다. `InventoryStatusSet`(`AllocStatus × QualityStatus × LocStatus`)은 생성 시 잘못된 조합이면 예외를 던진다.

## 영속성

JPA Entity는 `adapter/out/persistence`에 있고 패키지 접근(package-private)이다. 도메인 모델과 분리되어 있으며 PersistenceAdapter가 `fromDomain()` / `toDomain()`으로 변환한다.

- Entity는 `BaseEntity`를 상속한다 (`createdBy`, `createdAt`, `updatedBy`, `updatedAt` 감사 필드).
- Entity는 `protected` 기본 생성자를 가진다.
- 변환이 detached 방식이므로 **JPA dirty checking이 동작하지 않는다.** 도메인 객체를 변경하면 반드시 `port.save()`를 명시 호출한다. 이를 놓치면 변경이 DB에 반영되지 않는다 (재고 분할 시 원본 차감이 유실되어 재고가 부풀려진 사고가 있었다). 서비스 코드를 쓰거나 리뷰할 때는 "변경 후 save 누락"을 항상 확인한다.
- 동시성이 필요한 재고 조회는 `findByIdForUpdate`처럼 포트에 비관적 락 조회를 별도로 노출한다.
- 스키마는 Flyway(`src/main/resources/db/migration`)로만 바꾼다. `ddl-auto: validate`이므로 Entity와 마이그레이션이 어긋나면 기동 시 실패한다.
- 컬렉션을 조회할 때는 N+1과 과다 조회를 확인한다. 최적화를 제안할 때는 기대 효과를 함께 설명한다.

## 에러 처리

```
ErrorCode (interface, global.error)
  └─ {Domain}ErrorCode (enum, {domain}/domain/exception)   # ErrorType + code + message
       └─ {Name}Exception extends BusinessException         # ErrorCode를 담아 던진다
GlobalExceptionHandler: BusinessException → ErrorResponseDto { errorCode, message, timestamp }
```

- `ErrorType`(`INVALID_REQUEST`, `NOT_FOUND`, `CONFLICT`, `RULE_VIOLATION` 등)은 오류의 분류다. 도메인은 HTTP 상태를 모른다. 변환은 `GlobalExceptionHandler`가 한다.
- 새 오류는 해당 도메인의 `{Domain}ErrorCode`에 항목을 추가하고 전용 예외 클래스를 만든다. `RuntimeException`이나 `IllegalArgumentException`을 직접 던지지 않는다. 값 객체와 `InventoryStatusSet`의 생성 시 검증도 전용 예외(`InvalidInventoryStatusCombinationException` 등)를 던진다.
- 에러 메시지는 한국어, 에러 코드는 `UPPER_SNAKE_CASE`다.

## DTO

- 요청/응답 DTO는 `record`다. `adapter/in/web`에 둔다.
- UseCase 입출력은 `application/port/in`의 `Command` / `Result` record다. 웹 DTO가 `toCommand()`로 변환한다.
- 도메인 모델을 응답으로 직접 반환하지 않는다.
- 입력 검증은 Bean Validation으로 형식을 검사하되, 도메인 불변식은 도메인에서 다시 강제한다 (웹 DTO에만 있는 규칙은 다른 진입점에서 우회된다).

## Spring

- 생성자 주입만 쓴다 (`@RequiredArgsConstructor`). 필드 주입 금지.
- 컨트롤러는 요청/응답 처리만 한다. 트랜잭션 경계(`@Transactional`)는 application service에 둔다.
- 설정값은 `application.yaml`에서 환경변수로 받는다. 비밀 정보는 기본값을 두지 않는다.

## 네이밍

- 포트: `{Noun}Port`(out), `{Verb}{Noun}UseCase`(in). 구현체: `{Noun}PersistenceAdapter`, `{Noun}Service`.
- 이벤트: 과거형 `{Noun}{Verb-ed}Event` (`InboundCompletedEvent`). 이벤트 수신 어댑터: `{Event}Adapter` 또는 `{Domain}EventAdapter`.
- 이름이 의도를 드러내지 못하면 바꾼다. 약어와 `Manager`/`Helper`/`Util` 같은 모호한 이름을 피한다.

## 주석

- 코드로 표현할 수 없는 제약이나 의도만 쓴다.
- 코드가 하는 일을 반복하는 주석을 쓰지 않는다.
- 시점/계획을 가리키는 주석("나중에", "Step 2에서 이동")을 쓰지 않는다. 마이그레이션 계획은 ADR이나 PR에 둔다.

## 리뷰 관점

코드를 리뷰하거나 설계를 제안할 때 다음을 본다. 정상 동작하는 코드라도 아래 문제는 지적한다.

- SRP 위반, 과도한 책임의 Service, God Object
- 계층 경계/의존 방향 위반, 숨은 결합
- 트랜잭션 경계 오류
- 빈약한 도메인 모델(Anemic), setter 남용, 캡슐화 부족
- 엣지 케이스, 숨은 버그, 확장성

지적할 때는 치명적인 문제와 선택적 개선을 구분하고, 대안과 trade-off를 함께 설명한다. 스타일만을 이유로 리팩토링을 권하지 않는다.
