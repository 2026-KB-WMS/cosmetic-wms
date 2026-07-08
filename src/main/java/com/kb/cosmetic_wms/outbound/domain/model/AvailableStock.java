package com.kb.cosmetic_wms.outbound.domain.model;

/**
 * FEFO 할당 대상이 되는 가용 재고 조각. 유통기한 임박 순으로 정렬되어 전달된다.
 */
public record AvailableStock(
        Long inventoryId,
        int availableQuantity
) {
}
