package com.kb.ordering.product.application.port.in.dto;

import com.kb.ordering.product.domain.product.enums.TemperatureType;

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