package com.kb.cosmetic_wms.product.application.port.out;

import com.kb.cosmetic_wms.product.domain.model.ProductType;

import java.util.Optional;

public interface ProductTypePort {
    Optional<ProductType> findById(Long productTypeId);
}