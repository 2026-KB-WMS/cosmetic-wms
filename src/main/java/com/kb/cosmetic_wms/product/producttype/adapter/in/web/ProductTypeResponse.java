package com.kb.cosmetic_wms.product.producttype.adapter.in.web;

import com.kb.cosmetic_wms.product.producttype.application.port.in.ProductTypeResult;

public record ProductTypeResponse(
        Long productTypeId,
        String typeCode,
        String typeName
) {
    public static ProductTypeResponse from(ProductTypeResult result) {
        return new ProductTypeResponse(result.productTypeId(), result.typeCode(), result.typeName());
    }
}
