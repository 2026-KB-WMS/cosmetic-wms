package com.kb.cosmetic_wms.product.domain.enums;

import lombok.Getter;

@Getter
public enum TemperatureType {
    ROOM("R", "상온"),
    COOL("C", "냉장");

    private final String shortCode;
    private final String description;

    TemperatureType(String shortCode, String description) {
        this.shortCode = shortCode;
        this.description = description;
    }
}