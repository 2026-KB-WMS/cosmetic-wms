package com.kb.ordering.product.domain.category.valueobject;

import com.kb.ordering.product.domain.category.exception.InvalidCategoryCodeException;
import com.kb.ordering.product.domain.shared.CodeValidator;

public record CategoryCode(String value) {

    public static final int REQUIRED_LENGTH = CodeValidator.REQUIRED_LENGTH;

    public CategoryCode {
        CodeValidator.validate(value,
                () -> new InvalidCategoryCodeException("카테고리 코드는 필수 입력 항목입니다."),
                () -> new InvalidCategoryCodeException("카테고리 코드는 " + REQUIRED_LENGTH + "자리여야 합니다."),
                () -> new InvalidCategoryCodeException("카테고리 코드는 영문 대문자만 가능합니다.")
        );
    }
}