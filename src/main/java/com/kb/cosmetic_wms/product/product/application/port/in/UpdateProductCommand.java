package com.kb.cosmetic_wms.product.product.application.port.in;

import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;

public record UpdateProductCommand(
        String productName,
        int productPrice,
        TemperatureType temperatureType,
        String skinType,
        String functionType,
        String ingredients,
        String cautions,
        String storageCondition
) {
}