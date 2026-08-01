package com.kb.ordering.product.application.port.in.dto;

import com.kb.ordering.product.domain.producttype.model.ProductType;

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
