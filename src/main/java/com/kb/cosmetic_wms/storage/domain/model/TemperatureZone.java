package com.kb.cosmetic_wms.storage.domain.model;

import com.kb.cosmetic_wms.storage.domain.exception.StorageErrorCode;
import com.kb.cosmetic_wms.storage.domain.exception.StorageValidationException;
import lombok.Getter;

import java.util.Arrays;

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

    public static TemperatureZone fromShortCode(String shortCode) {
        return Arrays.stream(values())
                .filter(zone -> zone.shortCode.equals(shortCode))
                .findFirst()
                .orElseThrow(() -> new StorageValidationException(StorageErrorCode.INVALID_TEMPERATURE_CODE));
    }
}
