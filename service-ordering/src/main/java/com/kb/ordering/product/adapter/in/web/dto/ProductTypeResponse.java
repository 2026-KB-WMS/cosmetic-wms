package com.kb.ordering.product.adapter.in.web.dto;

import com.kb.ordering.product.application.port.in.dto.ProductTypeResult;

public record ProductTypeResponse(
        Long productTypeId,
        String typeCode,
        String typeName
) {
    public static ProductTypeResponse from(ProductTypeResult result) {
        return new ProductTypeResponse(result.productTypeId(), result.typeCode(), result.typeName());
    }
}
