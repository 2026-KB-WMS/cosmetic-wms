# ADR-0002: SKU 코드를 상품 PK 기반 `P%06d`로 결정

- 상태: Accepted

## 배경

기존 복합 포맷(`BIO-SKN-TON-150-0001`)은 `sku_sequence` 테이블과 비관적 락이 필요했고, 일련번호 9999 오버플로우 한계가 있었으며 상품 속성이 바뀌면 코드의 의미가 어긋났다.

## 결정

SKU 코드는 `P%06d` 형식(예: `P000001`)이며 상품 PK에서 파생한다. DB `AUTO_INCREMENT`가 유일성을 보장하므로 별도 동시성 제어를 두지 않는다. `sku_sequence` 관련 코드는 삭제했다.

## 결과

- `Product.create()`는 `skuCode=null`로 만들고, 저장 후 `Product.assignSkuCode(productId)`를 호출한다.
- `ProductService.register()`는 INSERT → `assignSkuCode` → UPDATE의 2단계 저장이다.
- `ProductEntity.skuCode`는 첫 INSERT 때문에 nullable이다.
- SKU 코드는 상품 속성을 담지 않는다. 속성은 별도 필드로 조회한다.
- SKU 관련 신규 코드는 이 패턴을 따른다.
