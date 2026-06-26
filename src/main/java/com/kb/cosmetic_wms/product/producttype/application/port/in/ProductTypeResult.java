package com.kb.cosmetic_wms.product.producttype.application.port.in;

import com.kb.cosmetic_wms.product.producttype.domain.model.ProductType;

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
