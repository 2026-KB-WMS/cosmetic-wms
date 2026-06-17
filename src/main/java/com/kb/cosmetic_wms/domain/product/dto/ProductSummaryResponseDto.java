package com.kb.cosmetic_wms.domain.product.dto;

import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;

public record ProductSummaryResponseDto(
        Long id,
        String skuCode,
        String brandName,
        String productName,
        int productPrice,
        TemperatureType temperatureType
) {
    public static ProductSummaryResponseDto from(Product product) {
        return new ProductSummaryResponseDto(
                product.getId(),
                product.getSkuCode(),
                product.getBrandName(),
                product.getProductName(),
                product.getProductPrice(),
                product.getTemperatureType()
        );
    }
}
