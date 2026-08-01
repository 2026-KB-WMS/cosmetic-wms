package com.kb.ordering.product.application.port.in.dto;

import com.kb.ordering.product.domain.product.enums.TemperatureType;

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