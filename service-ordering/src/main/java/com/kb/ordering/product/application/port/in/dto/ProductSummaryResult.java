package com.kb.ordering.product.application.port.in.dto;

import com.kb.ordering.product.domain.product.enums.TemperatureType;
import com.kb.ordering.product.domain.product.model.Product;

public record ProductSummaryResult(
        Long productId,
        String skuCode,
        String brandName,
        String productName,
        int productPrice,
        TemperatureType temperatureType
) {
    public static ProductSummaryResult from(Product product) {
        return new ProductSummaryResult(
                product.getProductId(),
                product.getSkuCode(),
                product.getBrandName(),
                product.getProductName(),
                product.getProductPrice(),
                product.getTemperatureType()
        );
    }
}