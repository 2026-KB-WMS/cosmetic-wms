package com.kb.cosmetic_wms.product.application.port.in;

import com.kb.cosmetic_wms.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.product.domain.model.Product;

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