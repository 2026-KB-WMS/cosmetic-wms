package com.kb.ordering.product.application.port.in;

import com.kb.ordering.product.application.port.in.dto.ProductResult;
import com.kb.ordering.product.application.port.in.dto.ProductSummaryResult;

import java.util.Collection;
import java.util.List;

public interface FindProductUseCase {
    List<ProductSummaryResult> findAll();

    ProductResult findById(Long productId);

    boolean existsById(Long productId);

    boolean allExistByIds(Collection<Long> productIds);
}