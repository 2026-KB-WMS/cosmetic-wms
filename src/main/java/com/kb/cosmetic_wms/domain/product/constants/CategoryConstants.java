package com.kb.cosmetic_wms.domain.product.constants;

import java.util.regex.Pattern;

public final class CategoryConstants {

    private CategoryConstants() {
    }

    // Category Code
    public static final int CATEGORY_CODE_LENGTH = 3;

    // Regex
    public static final Pattern CATEGORY_CODE_PATTERN =
            Pattern.compile("^[A-Z]+$");

    // Validation Messages
    public static final String CATEGORY_CODE_REQUIRED_MESSAGE =
            "카테고리 코드는 필수 입력 항목입니다.";

    public static final String INVALID_CATEGORY_CODE_LENGTH_MESSAGE =
            "카테고리 코드는 " + CATEGORY_CODE_LENGTH + "자리여야 합니다.";

    public static final String INVALID_CATEGORY_CODE_FORMAT_MESSAGE =
            "카테고리 코드는 영문 대문자만 가능합니다.";

    public static final String CATEGORY_NAME_REQUIRED_MESSAGE =
            "카테고리 이름은 필수 입력 항목입니다.";

}
