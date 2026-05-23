package com.kb.cosmetic_wms.domain.product;

import java.util.regex.Pattern;

public final class ProductTypeConstants {

    private ProductTypeConstants() {
    }

    public static final int PRODUCT_TYPE_CODE_LENGTH = 3;

    // Regex
    public static final Pattern PRODUCT_TYPE_CODE_PATTERN =
            Pattern.compile("^[A-Z]+$");

    // Validation Messages
    public static final String PRODUCT_TYPE_CODE_REQUIRED_MESSAGE = "타입 코드는 필수 입력 항목입니다.";
    public static final String INVALID_PRODUCT_TYPE_CODE_LENGTH_MESSAGE = "타입 코드는 3자리여야 합니다.";
    public static final String INVALID_PRODUCT_TYPE_CODE_FORMAT_MESSAGE = "타입 코드는 영문 대문자만 가능합니다.";
    public static final String PRODUCT_TYPE_NAME_REQUIRED_MESSAGE = "타입 이름은 필수 입력 항목입니다.";
}
