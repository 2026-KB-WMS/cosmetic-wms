package com.kb.ordering.product.domain.producttype.valueobject;

import com.kb.ordering.product.domain.producttype.exception.InvalidTypeCodeException;
import com.kb.ordering.product.domain.shared.CodeValidator;

public record TypeCode(String value) {

    public static final int REQUIRED_LENGTH = CodeValidator.REQUIRED_LENGTH;

    public TypeCode {
        CodeValidator.validate(value,
                () -> new InvalidTypeCodeException("타입 코드는 필수 입력 항목입니다."),
                () -> new InvalidTypeCodeException("타입 코드는 " + REQUIRED_LENGTH + "자리여야 합니다."),
                () -> new InvalidTypeCodeException("타입 코드는 영문 대문자만 가능합니다.")
        );
    }
}