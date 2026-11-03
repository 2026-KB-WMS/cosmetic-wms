package com.kb.cosmetic_wms.product.product.application.port.in;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FindProductUseCase {
    List<ProductSummaryResult> findAll();

    ProductResult findById(Long productId);

    boolean existsById(Long productId);

    boolean allExistByIds(Collection<Long> productIds);

    /**
     * 상품별 보관 온도 구분 코드(R: 상온, C: 냉장)를 반환한다.
     */
    Map<Long, String> findTemperatureCodesByIds(Collection<Long> productIds);
}