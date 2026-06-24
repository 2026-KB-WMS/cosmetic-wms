package com.kb.cosmetic_wms.product.product.application.port.in;

import java.util.List;

public interface FindProductUseCase {
    List<ProductSummaryResult> findAll();

    ProductResult findById(Long productId);

    boolean existsById(Long productId);
}