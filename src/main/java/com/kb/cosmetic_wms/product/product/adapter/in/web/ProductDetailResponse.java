package com.kb.cosmetic_wms.product.product.adapter.in.web;

import com.kb.cosmetic_wms.product.product.application.port.in.ProductResult;
import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;

public record ProductDetailResponse(
        Long id,
        String skuCode,
        String brandName,
        String productName,
        int productPrice,
        TemperatureType temperatureType,
        ProductInfoResponse productInfo
) {

    public record ProductInfoResponse(
            String skinType,
            String functionType,
            int volumeValue,
            String volumeUnit,
            String ingredients,
            String cautions,
            String storageCondition
    ) {
    }

    public static ProductDetailResponse from(ProductResult result) {
        return new ProductDetailResponse(
                result.productId(),
                result.skuCode(),
                result.brandName(),
                result.productName(),
                result.productPrice(),
                result.temperatureType(),
                new ProductInfoResponse(
                        result.skinType(),
                        result.functionType(),
                        result.volumeValue(),
                        result.volumeUnit(),
                        result.ingredients(),
                        result.cautions(),
                        result.storageCondition()
                )
        );
    }
}