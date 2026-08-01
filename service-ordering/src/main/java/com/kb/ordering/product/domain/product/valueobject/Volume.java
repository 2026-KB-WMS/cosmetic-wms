package com.kb.ordering.product.domain.product.valueobject;

import com.kb.ordering.product.domain.product.exception.InvalidVolumeException;
import com.kb.ordering.product.domain.product.exception.InvalidVolumeUnitException;

import java.util.List;

public record Volume(int value, String unit) {

    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 10000;
    public static final String ALLOWED_UNITS_REGEX = "^(ml|g|ea|oz|fl\\.oz)$";
    private static final List<String> ALLOWED_UNITS = List.of("ml", "g", "ea", "oz", "fl.oz");

    public Volume {
        validateValue(value);
        unit = validateUnit(unit);
    }

    public static Volume of(int value, String unit) {
        return new Volume(value, unit);
    }

    private static void validateValue(int value) {
        if (value <= MIN_VALUE) {
            throw new InvalidVolumeException("화장품 용량은 " + MIN_VALUE + "보다 커야 합니다.");
        }
        if (value > MAX_VALUE) {
            throw new InvalidVolumeException("올바르지 않은 대용량 수치입니다. (최대 " + MAX_VALUE + "까지 허용)");
        }
    }

    private static String validateUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            throw new InvalidVolumeUnitException("용량 단위는 필수 입력 항목입니다.");
        }
        String normalized = unit.toLowerCase().replace(" ", "");
        if (!ALLOWED_UNITS.contains(normalized)) {
            throw new InvalidVolumeUnitException("올바르지 않은 용량 단위입니다. (ml, g, ea, oz, fl.oz 허용)");
        }
        return normalized;
    }
}