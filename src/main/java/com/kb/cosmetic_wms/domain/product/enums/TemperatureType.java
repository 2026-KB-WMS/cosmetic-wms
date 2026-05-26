package com.kb.cosmetic_wms.domain.product.enums;

public enum TemperatureType {
    ROOM("상온"),
    COOL("냉장");

    private final String description;

    TemperatureType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
