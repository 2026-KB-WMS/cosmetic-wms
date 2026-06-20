# 📑 CHANGELOG: Cosmetic WMS Project Documents

이 파일은 `docs/` 폴더 내 프로젝트 설계 문서 및 정책의 모든 주요 변경 사항을 기록한다.

## 📌 작성 규칙

1. **버전 형식**:
    - `Major (1.0.0)`: 아키텍처의 큰 변화, 프로젝트 방향성 변경
    - `Minor (1.1.0)`: 새로운 테이블 추가, 비즈니스 시나리오/정책 추가
    - `Patch (1.1.1)`: 단순 오타 수정, 명세서 설명 보완
2. **날짜 형식**: `YYYY-MM-DD` (예: 2026-05-08)
3. **최신순 정렬**: 가장 최근 버전이 문서 상단에 위치하도록 기록한다.

<br>

## 🏷️ 분류 태그 (Types of changes)

변경 내용에 따라 아래 태그 중 하나를 선택하여 작성한다.

- `Added`: 새로운 기능이나 문서 섹션이 추가된 경우
- `Changed`: 기존 기능이나 설계 명세가 변경된 경우
- `Fixed`: 오류, 오타 또는 설계상의 결함을 수정한 경우
- `Removed`: 항목을 완전히 제거한 경우
- `Security`: 보안 관련 취약점을 개선한 경우

---

<br>

### [1.3.0] - 2026-06-20

#### Changed

- **[DB] `quality_inspection` 테이블 구조 재설계**
    - `inbound_item_id` (FK → inbound_item) 컬럼 제거
    - `inspector_id` (FK → member) 컬럼 제거
    - `source_id BIGINT NOT NULL` 신규 추가 — 검사 요청 출처 도메인의 PK ID
    - `source_type VARCHAR(20) NOT NULL` 신규 추가 — 출처 도메인 구분 (`INBOUND` 등)
    - `status VARCHAR(20) NOT NULL` 신규 추가 — 검사 진행 상태 (`READY`, `IN_PROGRESS`, `COMPLETED`)
    - `inventory_id`: `NOT NULL` → nullable 완화 (검사 시점에 재고 미확정 허용)
    - `result`: `NOT NULL` → nullable 완화 (검사 미완료 상태에서 null 허용)
    - `fk_quality_inspection_inbound_item` FK 제거
    - Polymorphic Association 패턴 도입으로 입고 외 다른 도메인(반품, 리콜 등)의 검사 요청도 단일 테이블에서 처리 가능하도록 구조 개방

<br>

### [1.2.0] - 2026-06-18

#### Added

- **[ARCH] ADR(Architecture Decision Record) 문서 신규 도입**
    - 아키텍처 결정 사항을 추적하기 위한 ADR 문서 체계 수립.
    - Notion ADR 데이터베이스에 결정 완료된 3건 등록.

- **[ARCH] ADR-001: SKU 일련번호 생성 동시성 문제 해결** (2026-06-17)
    - `COUNT + 1` 방식의 두 가지 결함 확인:
        1. Race Condition — 동시 등록 시 동일 시퀀스 반환 → `DataIntegrityViolationException` (HTTP 500)
        2. 의미론적 결함 — 상품 삭제 후 재등록 시 번호 충돌
    - **결정**: `sku_sequence` 전용 테이블 도입 + `@Lock(LockModeType.PESSIMISTIC_WRITE)` 적용.
    - `ProductRepository.findNextSequence()` 및 `COUNT + 1` 로직 제거.

- **[ARCH] ADR-002: Inventory 유니크 제약조건 재설계** (2026-06-18)
    - 기존 UK `(product_id, lot_id, section_id)` 3컬럼 구성에서 상태 전환 시 분할(split)이 발생하면 UK 위반 오류 재현 확인.
    - **결정**: UK를 `(product_id, lot_id, section_id, alloc_status, quality_status, loc_status)` 6컬럼으로 변경.
    - `Inventory.mergeFrom()` 메서드 및 `findMergeTarget()` 쿼리 추가로 split 시 동일 상태 행 병합 처리.

- **[ARCH] ADR-003: Inventory 동시 allocate 요청 처리 — 동시성 제어 전략** (2026-06-18)
    - Lost Update 및 Over-allocation 발생 시나리오 확인.
    - **결정**: Pessimistic Locking 채택 (`findByIdForUpdate()` + `PESSIMISTIC_WRITE`).
    - 단일 트랜잭션에서 다수 행 락 획득 시 반드시 `id` 오름차순으로 호출하는 데드락 방지 규칙 명문화.

#### Changed

- **[DB] `sku_sequence` 테이블 신규 추가**
    - PK: `(brand_name, category_code, type_code, volume)`, 컬럼: `current_seq INT NOT NULL DEFAULT 0`.
    - 상품 등록 시 해당 SKU 조합의 시퀀스를 단조 증가(monotonically increasing) 방식으로 관리.

- **[DB] `inventory` 테이블 Unique Key 재설계**
    - 기존: `uk_inventory_unit (product_id, lot_id, section_id)`
    - 변경: `uk_inventory_unit (product_id, lot_id, section_id, alloc_status, quality_status, loc_status)`
    - 동일 상태 조합당 행 1개를 DB 레벨에서 보장하여 행 파편화(row fragmentation) 방지.

<br>

### [1.1.0] - 2026-05-12

#### Added

- **[DB] 반품-출고 간 참조 무결성 강화**
    - `Return` 테이블에 원본 출고 건을 식별하기 위한 `outbound_id` 컬럼 추가.
    - `outbound_id`에 대한 외래키(FK) 제약조건 설정 (`REFERENCES Outbound(outbound_id)`).

#### Changed

- **[POLICY] 반품 처리 정책 고도화**
    - 반품 신청 시 원본 출고 데이터(영수증) 참조를 필수화하여 '유령 반품' 방지 로직 수립.

#### Fixed

- **[DB] 데이터 모델 결함 수정**
    - 반품 마스터 테이블에서 원 주문/출고 이력을 추적할 수 없었던 논리적 단절 오류 해결.
    - 데이터 정합성 테스트를 통해 발견된 아키텍처 구조적 빈틈 보완.

<br>

### [1.0.0] - 2026-05-08

#### Added

- **[DB] 데이터베이스 설계 (v1)**
    - 총 28개의 엔티티 정의. (사용자/조직, 상품, 재고/로트, 입고, 출고, 창고 작업, 사후 관리 등)
    - 모든 테이블에 적용되는 공통 컬럼(`created_by, created_at, updated_by, updated_at`) 정의.

- **[SCENARIO] 비즈니스 시나리오 (v1)**
    - 주요 액터(총괄 관리자, 창고 관리자, 점주) 정의 및 역할 할당.
    - 화장품 도메인 특수성(유통기한 FEFO, 상온 보관 환경, 로트 추적) 반영.
    - 입/출고, 재고 이동, 리콜 및 폐기 처리 등 시스템 핵심 프로세스 수립.

- **[ARCH] 시스템 아키텍처 및 운영 가이드 (v1)**
    - 표준 식별 체계 정립 (SKU 코드, 로트 번호, 섹션 코드 조합).
    - 3차원 재고 상태 매트릭스 정의 (할당 상태, 품질 상태, 위치/이동 상태).
    - 상태 간 상호 의존성 및 정합성 검증 규칙 수립.
    - 가중치 기반 창고 배정 알고리즘 공식 정의.

- **[ARCH] 공통 ENUM 코드 (v1)**
    - 프로젝트 전반에서 사용될 코드 값 정립.
    - 각 코드들은 `enum` 클래스로 구현될 예정.