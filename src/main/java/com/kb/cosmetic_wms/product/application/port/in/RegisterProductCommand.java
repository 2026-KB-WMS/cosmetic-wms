package com.kb.cosmetic_wms.product.application.port.in;

import com.kb.cosmetic_wms.product.domain.enums.TemperatureType;

public record RegisterProductCommand(
        String brandName,
        String productName,
        int productPrice,
        TemperatureType temperatureType,
        Long categoryId,
        Long productTypeId,
        String skinType,
        String functionType,
        int volumeValue,
        String volumeUnit,
        String ingredients,
        String cautions,
        String storageCondition
) {
}