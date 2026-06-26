package com.kb.cosmetic_wms.product.producttype.application.port.out;

import com.kb.cosmetic_wms.product.producttype.domain.model.ProductType;

import java.util.List;
import java.util.Optional;

public interface ProductTypePort {
    Optional<ProductType> findById(Long productTypeId);
    List<ProductType> findAll();
    boolean existsByCode(String typeCode);
    ProductType save(ProductType productType);
    void deleteById(Long productTypeId);
}
