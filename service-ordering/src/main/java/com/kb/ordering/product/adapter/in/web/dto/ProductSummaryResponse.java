package com.kb.ordering.product.adapter.in.web.dto;

import com.kb.ordering.product.application.port.in.dto.ProductSummaryResult;
import com.kb.ordering.product.domain.product.enums.TemperatureType;

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