package com.kb.cosmetic_wms.product.category.domain.valueobject;

import com.kb.cosmetic_wms.product.category.domain.exception.InvalidCategoryCodeException;

import java.util.regex.Pattern;

public record CategoryCode(String value) {

    public static final int REQUIRED_LENGTH = 3;
    private static final Pattern UPPERCASE_ONLY = Pattern.compile("^[A-Z]+$");

    public CategoryCode {
        if (value == null || value.isBlank()) {
            throw new InvalidCategoryCodeException("카테고리 코드는 필수 입력 항목입니다.");
        }
        if (value.length() != REQUIRED_LENGTH) {
            throw new InvalidCategoryCodeException("카테고리 코드는 " + REQUIRED_LENGTH + "자리여야 합니다.");
        }
        if (!UPPERCASE_ONLY.matcher(value).matches()) {
            throw new InvalidCategoryCodeException("카테고리 코드는 영문 대문자만 가능합니다.");
        }
    }
}