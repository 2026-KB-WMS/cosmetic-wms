package com.kb.cosmetic_wms.lot.domain.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LotErrorCode implements ErrorCode {

    LOT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "LOT_NOT_FOUND",
            "존재하지 않는 로트입니다."
    ),
    PRODUCT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PRODUCT_NOT_FOUND",
            "존재하지 않는 상품입니다."
    ),
    DUPLICATE_LOT_NUMBER(
            HttpStatus.CONFLICT,
            "DUPLICATE_LOT_NUMBER",
            "이미 등록된 로트 번호입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}