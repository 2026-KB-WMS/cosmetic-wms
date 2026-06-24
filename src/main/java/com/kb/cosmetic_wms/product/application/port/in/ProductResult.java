package com.kb.cosmetic_wms.product.application.port.in;

import com.kb.cosmetic_wms.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.product.domain.model.Product;
import com.kb.cosmetic_wms.product.domain.model.ProductInfo;

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