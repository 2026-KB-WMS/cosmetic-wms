package com.kb.ordering.product.application.port.in;

import com.kb.ordering.product.application.port.in.dto.ProductTypeResult;

import java.util.List;

public interface FindProductTypeUseCase {
    List<ProductTypeResult> findAll();

    ProductTypeResult findById(Long productTypeId);
}
