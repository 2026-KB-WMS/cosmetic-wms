package com.kb.cosmetic_wms.product.application.port.in;

import java.util.List;

public interface FindProductTypeUseCase {
    List<ProductTypeResult> findAll();
    ProductTypeResult findById(Long productTypeId);
}