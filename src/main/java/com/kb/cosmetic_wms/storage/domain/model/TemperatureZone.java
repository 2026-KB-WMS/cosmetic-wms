package com.kb.cosmetic_wms.storage.domain.model;

import lombok.Getter;

@Getter
public enum TemperatureZone {
    ROOM("R", "상온"),
    COOL("C", "냉장");

    private final String shortCode;
    private final String description;

    TemperatureZone(String shortCode, String description) {
        this.shortCode = shortCode;
        this.description = description;
    }
}
