package com.kb.cosmetic_wms.product.application.port.in;

import com.kb.cosmetic_wms.product.domain.model.ProductType;

public record ProductTypeResult(
        Long productTypeId,
        String typeCode,
        String typeName
) {
    public static ProductTypeResult from(ProductType productType) {
        return new ProductTypeResult(
                productType.getProductTypeId(),
                productType.getTypeCode(),
                productType.getTypeName()
        );
    }
}