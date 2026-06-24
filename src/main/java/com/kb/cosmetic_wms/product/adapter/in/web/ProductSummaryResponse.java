package com.kb.cosmetic_wms.product.adapter.in.web;

import com.kb.cosmetic_wms.product.application.port.in.ProductSummaryResult;
import com.kb.cosmetic_wms.product.domain.enums.TemperatureType;

public record ProductSummaryResponse(
        Long id,
        String skuCode,
        String brandName,
        String productName,
        int productPrice,
        TemperatureType temperatureType
) {
    public static ProductSummaryResponse from(ProductSummaryResult result) {
        return new ProductSummaryResponse(
                result.productId(),
                result.skuCode(),
                result.brandName(),
                result.productName(),
                result.productPrice(),
                result.temperatureType()
        );
    }
}