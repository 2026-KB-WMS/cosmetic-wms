package com.kb.cosmetic_wms.domain.product.dto;

import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.product.entity.ProductInfo;
import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;

public record ProductDetailResponseDto(
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
    ) {}

    public static ProductDetailResponseDto from(Product product) {
        ProductInfo pi = product.getProductInfo();
        return new ProductDetailResponseDto(
                product.getId(),
                product.getSkuCode(),
                product.getBrandName(),
                product.getProductName(),
                product.getProductPrice(),
                product.getTemperatureType(),
                new ProductInfoResponse(
                        pi.getSkinType(),
                        pi.getFunctionType(),
                        pi.getVolume().value(),
                        pi.getVolume().unit(),
                        pi.getIngredients(),
                        pi.getCautions(),
                        pi.getStorageCondition()
                )
        );
    }
}
