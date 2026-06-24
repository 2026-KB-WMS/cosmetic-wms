package com.kb.cosmetic_wms.product.product.domain.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다."),
    SKU_SEQUENCE_OVERFLOW(HttpStatus.CONFLICT, "SKU_SEQUENCE_OVERFLOW", "SKU 일련번호가 최대값(9999)을 초과했습니다."),
    DUPLICATE_PRODUCT(HttpStatus.CONFLICT, "DUPLICATE_PRODUCT", "동일한 스펙의 상품이 이미 존재합니다."),
    INVALID_BRAND_NAME(HttpStatus.BAD_REQUEST, "INVALID_BRAND_NAME", "브랜드명은 필수 입력 항목입니다."),
    INVALID_PRODUCT_NAME(HttpStatus.BAD_REQUEST, "INVALID_PRODUCT_NAME", "상품명은 필수 입력 항목입니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "INVALID_PRICE", "상품 가격은 0원 이상이어야 합니다."),
    INVALID_TEMPERATURE_TYPE(HttpStatus.BAD_REQUEST, "INVALID_TEMPERATURE_TYPE", "보관 온도 타입은 필수 입력 항목입니다."),
    INVALID_PRODUCT(HttpStatus.BAD_REQUEST, "INVALID_PRODUCT", "올바르지 않은 상품 데이터입니다."),
    INVALID_VOLUME(HttpStatus.BAD_REQUEST, "INVALID_VOLUME", "올바르지 않은 용량 수치입니다."),
    INVALID_VOLUME_UNIT(HttpStatus.BAD_REQUEST, "INVALID_VOLUME_UNIT", "올바르지 않은 용량 단위입니다."),
    INVALID_PRODUCT_INFO(HttpStatus.BAD_REQUEST, "INVALID_PRODUCT_INFO", "올바르지 않은 상품 정보입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
