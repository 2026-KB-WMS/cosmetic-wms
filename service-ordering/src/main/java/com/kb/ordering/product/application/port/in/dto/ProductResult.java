package com.kb.ordering.product.application.port.in.dto;

import com.kb.ordering.product.domain.product.enums.TemperatureType;
import com.kb.ordering.product.domain.product.model.Product;
import com.kb.ordering.product.domain.product.model.ProductInfo;

public record ProductResult(
        Long productId,
        String skuCode,
        String brandName,
        String productName,
        int productPrice,
        TemperatureType temperatureType,
        String skinType,
        String functionType,
        int volumeValue,
        String volumeUnit,
        String ingredients,
        String cautions,
        String storageCondition
) {
    public static ProductResult from(Product product) {
        ProductInfo info = product.getProductInfo();
        return new ProductResult(
                product.getProductId(),
                product.getSkuCode(),
                product.getBrandName(),
                product.getProductName(),
                product.getProductPrice(),
                product.getTemperatureType(),
                info.getSkinType(),
                info.getFunctionType(),
                info.getVolume().value(),
                info.getVolume().unit(),
                info.getIngredients(),
                info.getCautions(),
                info.getStorageCondition()
        );
    }
}