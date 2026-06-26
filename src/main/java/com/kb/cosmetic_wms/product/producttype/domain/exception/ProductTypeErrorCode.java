package com.kb.cosmetic_wms.product.producttype.domain.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductTypeErrorCode implements ErrorCode {

    PRODUCT_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_TYPE_NOT_FOUND", "존재하지 않는 상품 타입입니다."),
    DUPLICATE_PRODUCT_TYPE(HttpStatus.CONFLICT, "DUPLICATE_PRODUCT_TYPE", "동일한 코드의 상품 타입이 이미 존재합니다."),
    PRODUCT_TYPE_IN_USE(HttpStatus.CONFLICT, "PRODUCT_TYPE_IN_USE", "해당 상품 타입을 참조하는 상품이 존재하여 삭제할 수 없습니다."),
    INVALID_TYPE_CODE(HttpStatus.BAD_REQUEST, "INVALID_TYPE_CODE", "타입 코드가 올바르지 않습니다."),
    INVALID_TYPE_NAME(HttpStatus.BAD_REQUEST, "INVALID_TYPE_NAME", "타입 이름은 필수 입력 항목입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}