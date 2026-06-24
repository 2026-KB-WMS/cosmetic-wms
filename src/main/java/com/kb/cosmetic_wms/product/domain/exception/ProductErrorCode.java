package com.kb.cosmetic_wms.product.domain.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "존재하지 않는 카테고리입니다."),
    PRODUCT_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_TYPE_NOT_FOUND", "존재하지 않는 상품 타입입니다."),
    SKU_SEQUENCE_OVERFLOW(HttpStatus.CONFLICT, "SKU_SEQUENCE_OVERFLOW", "SKU 일련번호가 최대값(9999)을 초과했습니다."),
    DUPLICATE_PRODUCT(HttpStatus.CONFLICT, "DUPLICATE_PRODUCT", "동일한 스펙의 상품이 이미 존재합니다."),
    DUPLICATE_CATEGORY(HttpStatus.CONFLICT, "DUPLICATE_CATEGORY", "동일한 코드의 카테고리가 이미 존재합니다."),
    DUPLICATE_PRODUCT_TYPE(HttpStatus.CONFLICT, "DUPLICATE_PRODUCT_TYPE", "동일한 코드의 상품 타입이 이미 존재합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}