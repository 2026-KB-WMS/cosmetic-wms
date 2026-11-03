# 도메인 용어와 식별 체계

화장품은 유통기한과 보관 온도에 민감하고, 품질 이슈 시 **로트 단위의 신속한 추적/회수**가 필수다. 시스템의 두 축은 다음과 같다.

- **Lot 단위 생애주기 추적**: 재고의 실질적 관리 단위는 로트다.
- **FEFO(First Expired, First Out)**: 출고 시 유통기한이 가장 임박한 로트부터 할당한다. 시스템이 강제한다.

> `docs/arch/v1.1.0_system_guidelines.md`는 초기 설계 문서이며 SKU/로트 번호/섹션 코드 형식이 현재 코드와 다르다. 형식은 이 문서와 ADR, 코드를 기준으로 한다.

## 액터 (Role)

역할별 접근 가능한 API는 [security.md](security.md)에 있다.

| 코드 | 설명 |
|---|---|
| `ROLE_HEADQUARTERS` | 본사 관리자. 전사 재고 이동, 리콜 명령, 시스템 정책 설정. **직접 가입 불가** (`Role.isSelfRegistrable()`, 가입 유스케이스가 거부). 최초 계정은 환경변수 부트스트랩으로 생성 |
| `ROLE_WAREHOUSE_MANAGER` | 창고 관리자. 입출고 검수, 로케이션 관리, 담당 창고 재고 처리 |
| `ROLE_FRANCHISE_MANAGER` | 점주. 발주, 반품, 유통기한 경과 상품 폐기 신청 |

## 식별 체계

| 구분 | 형식 | 예시 | 근거 |
|---|---|---|---|
| SKU 코드 | `P%06d` (상품 PK 기반) | `P000001` | [ADR-0002](../adr/0002-sku-format.md) |
| 로트 번호 | `YYMMDD-{입고ID}-{제조사 로트 번호}` | `260629-1042-LOT0001` | [ADR-0003](../adr/0003-lot-number-format.md) |
| 섹션 코드 | `WH{NN}-{구역타입}-{온도}-{순번NN}` | `WH01-STR-R-01` | `SectionCode` |
| 재고 식별키 | `Product + Lot + Section + InventoryStatusSet` | DB 복합 UK | [DB 설계서](../db/v1.6.0_database.md) |

- 섹션 코드 패턴: `^WH\d{2}-(DOCK|STR|QUAR)-[RC]-\d{2}$`
- 구역 타입(`SectionType`): `DOCKING`(DOCK, 반입/반출·검수), `STORAGE`(STR, 일반 보관), `QUARANTINE`(QUAR, 격리/폐기)
- 온도(`TemperatureZone`): `ROOM`(R, 상온), `COOL`(C, 냉장)
- 동일 SKU라도 유통기한/제조일자가 다르면 별개 로트다. 하나의 입고 건에는 하나의 로트 번호를 부여한다.

## 재고 상태 매트릭스

`InventoryStatusSet`은 세 축의 상태 조합이며, 생성 시 불가능한 조합을 거부한다 (`InvalidInventoryStatusCombinationException`).

| 축 | 값 |
|---|---|
| `AllocStatus` (할당) | `UNALLOCATED`, `ALLOCATED`, `SHIPPED` |
| `QualityStatus` (품질) | `NORMAL`, `INSPECTING`, `HOLD`, `DISCARD_SCHEDULED` |
| `LocStatus` (위치) | `STORED`, `MOVING`, `DOCKING` |

대표적인 금지 조합: 할당된 재고는 품질이 `NORMAL`이어야 한다. 이동 중(`MOVING`)이거나 `DOCKING`인 재고는 할당할 수 없다. 전체 규칙은 `InventoryStatusSet`이 사실이다.

재고는 상태가 달라지면 별도 행이다. 상태 전이 시 분할(`SplitResult`)/병합이 일어난다. 할당 시 원본이 감소하고 신규 `ALLOCATED` 레코드가 만들어질 수 있고, 이때 ID가 달라진다 ([events.md](events.md#재고-할당-id-추적)).

모든 재고 변동은 `InventoryTransaction`(`TransactionType`: `ALLOCATE`, `UNALLOCATE`, `SHIP`, `LOCATION_MOVE`, `INSPECTION_PASS`, `INSPECTION_FAIL`, `DISCARD`, `SPLIT_DEDUCT` 등)으로 기록된다.

## 문서별 상태 흐름

| 문서 | 상태 |
|---|---|
| 입고 `InboundStatus` | `SCHEDULED` → `RECEIVED` / `CANCELED` |
| 품질검사 `InspectionStatus` | `WAITING` → `IN_PROGRESS` → `COMPLETED` |
| 로트 `LotStatus` | `AVAILABLE`, `HOLD`, `EXPIRED`, `RECALLED`, `DAMAGED`, `DISPOSED` |
| 발주 `OrderStatus` | `PENDING` → `CONFIRMED` → `PREPARING` → `SHIPPED` → `DELIVERED` / `CANCELED` |
| 출고 `OutboundStatus` | `PENDING` → `ALLOCATED` → `PROCESSING` → `SHIPPED` / `CANCELED` |

전이 규칙은 각 도메인 모델의 메서드에 있다.

## 핵심 비즈니스 플로우

1. **입고**: 입고 예정 → 수령 완료(`InboundCompletedEvent`) → 로트 생성, DOCKING 용량 갱신, 품질검사 전표 생성
2. **품질검사**: 검사 완료(`InspectionCompletedEvent`) → 합격은 `NORMAL`, 불합격은 `HOLD`로 재고 반영 → 적재(putaway)
3. **발주→출고**: 발주 확정 → 최적 창고 배정(OMS, [ADR-0004](../adr/0004-warehouse-assignment-policy.md)) → 출고 전표 생성 + FEFO 할당 → 출하 시 재고 차감

이벤트 구독 관계는 [events.md](events.md)를 참고한다.

## 외부 연동

- 지오코딩/라우팅은 Kakao API를 쓴다. `KAKAO_REST_API_KEY` 환경변수가 없으면 창고·가맹점 등록이 502로 실패한다.
