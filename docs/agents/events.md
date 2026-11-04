# 도메인 이벤트

도메인 간 비동기 협력은 Spring 이벤트로 한다. 발행은 `global.event.EventPublisher` 포트를 통한다 (구현: `SpringEventPublisher`).

## 위치 원칙

- 이벤트 record는 **발행하는 도메인**의 `domain/event`에 둔다 (예: `inbound.domain.event.InboundCompletedEvent`).
- 구독하는 도메인은 `adapter/in/event`에 수신 어댑터를 두고 상대 도메인의 이벤트를 import한다. 이것이 도메인 간 import의 허용 경로다.
- 이벤트 payload는 스냅샷(ID와 값)이다. 도메인 모델/엔티티 참조를 담지 않는다.

## 리스너 규칙

두 가지 방식이 쓰이며 목적이 다르다.

| 방식 | 어노테이션 | 용도 |
|---|---|---|
| 같은 트랜잭션 | `@TransactionalEventListener(phase = BEFORE_COMMIT)` | 발행 측과 원자적으로 처리해야 하는 후속 작업 (재고 반영, 로트 생성, 발주 상태 전이) |
| 커밋 후 비동기 | `@Async("eventTaskExecutor")` + `@TransactionalEventListener(phase = AFTER_COMMIT)` | 실패해도 발행 측을 롤백하지 않는 작업 (품질검사 전표 생성, 창고 배정) |

비동기 리스너는 `@Retryable`을 가진 별도 빈(`Retryable*`)으로 위임해 재시도하고, 최종 실패는 기록한다 (`RetryableInspectionCreator`, `RetryableWarehouseAssigner`).

## 금지: BEFORE_COMMIT 중첩 발행

`BEFORE_COMMIT` 리스너 안에서 발행한 이벤트의 `BEFORE_COMMIT` 리스너는 **실행되지 않는다.** Spring의 `triggerBeforeCommit`이 synchronization 스냅숏을 순회하기 때문이다. 오류 없이 조용히 누락된다.

- 이 구간에서 다음 단계를 호출해야 하면 이벤트 대신 out port를 직접 호출한다 (예: `CreateOutboundFromAssignmentService`).
- 이런 체인을 이벤트로 "일원화"하는 리팩토링은 하지 않는다.

## 주요 플로우

```
입고 완료 (InboundCompletedEvent)
 ├─ lot: 로트 생성          (BEFORE_COMMIT)
 ├─ storage: DOCKING 용량 갱신 (BEFORE_COMMIT)
 └─ inspection: 품질검사 전표 생성 (AFTER_COMMIT, @Async)
품질검사 완료 (InspectionCompletedEvent) → inventory: 재고 반영 (합격→NORMAL, 불합격→HOLD)

발주 확정 (OrderConfirmedEvent) → oms: 최적 창고 배정 (AFTER_COMMIT, @Async)
창고 배정 (WarehouseAssignedEvent) → outbound: 출고 전표 생성·FEFO 할당 (BEFORE_COMMIT)
OutboundAllocatedEvent → inventory: ALLOCATE / order: 준비 중 전환
OutboundShippedEvent → inventory: 재고 차감 / order: 출하 전환
OutboundStockReleaseRequestedEvent → inventory: 할당 해제
```

정확한 구독 관계는 각 도메인의 `adapter/in/event`를 확인한다 (코드가 사실이다).

## 실패한 이벤트의 복구

비동기 리스너가 재시도(3회)를 모두 실패하면 `@Recover`가 실패 기록을 남긴다. 기록은 읽기만 하는 로그가 아니라 **본사 관리자가 조회하고 재처리하는 작업 목록**이다.

| 실패한 작업 | 기록 테이블 | API (본사 전용) |
|---|---|---|
| 발주 확정 후 창고 배정 | `failed_assignment_event` | `/api/v1/admin/failed-assignments` |
| 입고 완료 후 검사 전표 생성 | `failed_inspection_event` | `/api/v1/admin/failed-inspections` |

- `GET ?status=PENDING`(기본): 처리 대기 건 조회. 상태는 `PENDING` → `RESOLVED`(재처리 성공) / `DISCARDED`(폐기).
- `POST /{id}/retry`: 실패 지점부터 다시 시도한다. **시도가 실패해도 HTTP 200**이며, `status`가 `PENDING`으로 남고 `retryCount`와 `errorMessage`(최근 사유)가 갱신된다. 이미 처리된 건은 409다.
- `POST /{id}/discard`: 더 이상 처리할 필요가 없는 건을 닫는다.
- 창고 배정에 계속 실패하는 발주는 재고를 채워 재처리하거나, 창고가 배정되지 않았다면 발주를 취소한다. 발주를 취소해도 대기 중인 실패 기록은 자동으로 닫히지 않는다. 재처리하면 사유(배정 대기 상태가 아님)가 남으므로 `discard`로 닫는다.

구현 원칙:
- 재처리는 실패 기록 행을 `PESSIMISTIC_WRITE`로 잠그고 시작한다. 같은 건의 동시 재처리는 직렬화되어 한 번만 처리된다.
- 실제 작업은 `NewTransactionRunner`로 **별도 트랜잭션**에서 실행한다. 작업이 롤백되어도 실패 기록(시도 횟수, 사유)은 남는다.
- 재처리는 멱등해야 한다. 창고 배정은 발주가 `CONFIRMED`이고 창고가 비어 있을 때만 시도하고(외부 Routing API 호출 전에 확인), 검사 전표는 같은 입고 라인의 전표가 이미 있으면 만들지 않고 해결 처리한다.
- 새 비동기 리스너에 `@Recover`를 추가할 때는 같은 구조(상태 있는 실패 기록 + 재처리 API)로 만든다.

## 재고 할당 ID 추적

`Inventory.allocate(qty)`는 `qty < quantity`이면 분할(`SplitResult`)되어 새 ALLOCATED 레코드가 **다른 ID**를 받는다. `OutboundItem.inventoryId`는 FEFO 선택 시점의 ID라 할당 후 실제 ALLOCATED 재고 ID와 다를 수 있다.

출하/취소 시에는 `InventoryTransaction`에서 `type=ALLOCATE AND referenceId=outboundId`를 조회해 실제 재고를 특정한다.

## 새 이벤트를 추가할 때

1. 발행 도메인의 `domain/event`에 record를 만든다 (스냅샷 필드만).
2. 구독 도메인의 `adapter/in/event`에 수신 어댑터를 만든다. 리스너 방식을 위 표에서 고른다.
3. BEFORE_COMMIT 체인에 끼어드는지 확인한다 (중첩 발행 금지).
4. 수신 어댑터와 그 효과를 검증하는 테스트를 추가한다. 플로우가 도메인을 가로지르면 `integration/`에 통합 테스트를 둔다.
