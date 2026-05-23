package com.kb.cosmetic_wms.domain.product.entity;

import com.kb.cosmetic_wms.domain.product.ProductInfoConstants;
import jakarta.persistence.Embeddable;

@Embeddable
public record Volume(int value, String unit) {

    public Volume {
        validateValue(value);
        unit = validateUnit(unit);
    }

    public static Volume of(int value, String unit) {
        return new Volume(value, unit);
    }

    private static void validateValue(int value) {
        if (value <= ProductInfoConstants.MIN_VOLUME) {
            throw new IllegalArgumentException(ProductInfoConstants.INVALID_VOLUME_MIN_MESSAGE);
        }
        if (value > ProductInfoConstants.MAX_VOLUME) {
            throw new IllegalArgumentException(ProductInfoConstants.INVALID_VOLUME_MAX_MESSAGE);
        }
    }

    private static String validateUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException(ProductInfoConstants.UNIT_REQUIRED_MESSAGE);
        }

        String normalizedUnit = unit.toLowerCase().replace(" ", "");

        if (!ProductInfoConstants.ALLOWED_UNITS.contains(normalizedUnit)) {
            throw new IllegalArgumentException(ProductInfoConstants.INVALID_UNIT_FORMAT_MESSAGE);
        }

        return normalizedUnit;
    }
}
