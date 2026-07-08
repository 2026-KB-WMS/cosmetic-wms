package com.kb.cosmetic_wms.outbound.application.port.out;

import com.kb.cosmetic_wms.outbound.domain.model.AvailableStock;

import java.util.List;

public interface FefoInventoryQueryPort {

    /**
     * 상품·창고 기준 가용 재고를 유통기한 임박 순으로 조회한다.
     */
    List<AvailableStock> findAvailableForFefo(Long productId, Long warehouseId);
}
