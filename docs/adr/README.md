# Architecture Decision Records

되돌리기 어렵거나 코드만으로 이유를 알 수 없는 설계 결정을 기록한다. 번호는 순차이며 한 번 정한 번호는 바꾸지 않는다. 결정이 바뀌면 기존 ADR을 지우지 않고 상태를 `Superseded`로 바꾸고 새 ADR을 추가한다.

| 번호 | 제목 | 상태 |
|---|---|---|
| [0001](0001-single-module-monolith.md) | 단일 모듈 모놀리스 유지 | Accepted |
| [0002](0002-sku-format.md) | SKU 코드를 상품 PK 기반 `P%06d`로 결정 | Accepted |
| [0003](0003-lot-number-format.md) | 로트 번호를 입고 기반 복합 식별자로 변경 | Accepted |
| [0004](0004-warehouse-assignment-policy.md) | 창고 배정 정책 (가중치 기반) | Accepted |

## 템플릿

```markdown
# ADR-NNNN: 제목

- 상태: Proposed | Accepted | Superseded by ADR-XXXX
- 날짜: YYYY-MM-DD

## 배경
## 결정
## 결과 (trade-off 포함)
```
