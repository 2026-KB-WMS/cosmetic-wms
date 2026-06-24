package com.kb.cosmetic_wms.product.category.domain.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCode implements ErrorCode {

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "존재하지 않는 카테고리입니다."),
    DUPLICATE_CATEGORY(HttpStatus.CONFLICT, "DUPLICATE_CATEGORY", "동일한 코드의 카테고리가 이미 존재합니다."),
    CATEGORY_IN_USE(HttpStatus.CONFLICT, "CATEGORY_IN_USE", "해당 카테고리를 참조하는 상품이 존재하여 삭제할 수 없습니다."),
    INVALID_CATEGORY_CODE(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY_CODE", "카테고리 코드가 올바르지 않습니다."),
    INVALID_CATEGORY_NAME(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY_NAME", "카테고리 이름은 필수 입력 항목입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}