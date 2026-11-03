# ADR-0003: 로트 번호를 입고 기반 복합 식별자로 변경

- 상태: Accepted
- 날짜: 2026-06-29

## 배경

시스템이 생성한 로트 번호(`SKN-260507-01-0024`)가 제조사 실물 로트 번호와 달라, 리콜 대응 시 대사가 불가능했다.

## 결정

로트 번호를 `YYMMDD-{입고ID}-{제조사 로트 번호}`로 한다 (예: `260629-1042-LOT0001`). 시스템 생성 방식은 폐기했다.

제조사 로트 번호 제약:
- 허용 문자: 영문 대문자, 숫자, 하이픈. 최대 20자 (GS1 Lot/Batch Number 기준).
- 전체 정규식: `^\d{6}-\d{1,19}-[A-Z0-9][A-Z0-9\-]{0,19}$`
- 제조사 로트 번호 단독: `^[A-Z0-9][A-Z0-9\-]{0,19}$`

## 결과

- 입고 ID가 번호에 들어가므로 유일성이 보장되고, Lot 생성 진입점이 Inbound 플로우로 구조적으로 강제된다.
- `Lot.create(inboundDate, inboundId, manufacturerLotNumber, manufacturingDate, expirationDate, productId)`로 생성한다. `Lot.reconstitute()`는 DB 복원용으로 `String lotNumber`를 받는다.
- `RegisterLotRequest`는 `inboundDate + inboundId + manufacturerLotNumber` 세 필드를 받는다.
- 테스트 픽스처 기본값: `inboundDate=2026-01-01`, `inboundId=1`, `manufacturerLotNumber=LOT0001` → `260101-1-LOT0001`.
