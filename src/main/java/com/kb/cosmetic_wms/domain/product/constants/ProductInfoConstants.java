package com.kb.cosmetic_wms.domain.product.constants;

import java.util.List;

public class ProductInfoConstants {

    private ProductInfoConstants() {
    }

    // 용량 제한 경계값
    public static final int MIN_VOLUME = 0;
    public static final int MAX_VOLUME = 10000;

    public static final int SKIN_TYPE_MAX_LENGTH = 50;

    public static final int FUNCTION_TYPE_MAX_LENGTH = 100;

    public static final int STORAGE_CONDITION_MAX_LENGTH = 255;

    // 허용 용량 단위
    public static final List<String> ALLOWED_UNITS = List.of("ml", "g", "ea", "oz", "fl.oz");

    // Validation Messages
    public static final String INVALID_VOLUME_MIN_MESSAGE =
            "화장품 용량은 0보다 커야 합니다.";
    public static final String INVALID_VOLUME_MAX_MESSAGE =
            "올바르지 않은 대용량 수치입니다. (최대 10,000까지 허용)";

    public static final String UNIT_REQUIRED_MESSAGE =
            "용량 단위는 필수 입력 항목입니다.";
    public static final String INVALID_UNIT_FORMAT_MESSAGE =
            "올바르지 않은 용량 단위입니다. (ml, g, ea, oz, fl. oz 허용)";

    public static final String INVALID_SKIN_TYPE_LENGTH_MESSAGE =
            "피부 타입 정보는 50자를 초과할 수 없습니다.";

    public static final String INVALID_FUNCTION_TYPE_LENGTH_MESSAGE =
            "기능성 타입 정보는 100자를 초과할 수 없습니다.";

    public static final String INVALID_STORAGE_CONDITION_LENGTH_MESSAGE =
            "보관 조건 정보는 255자를 초과할 수 없습니다.";
}
