package com.kb.cosmetic_wms.lot.domain.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import com.kb.cosmetic_wms.global.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LotErrorCode implements ErrorCode {

    LOT_NOT_FOUND(
            ErrorType.NOT_FOUND,
            "LOT_NOT_FOUND",
            "존재하지 않는 로트입니다."
    ),
    PRODUCT_NOT_FOUND(
            ErrorType.NOT_FOUND,
            "PRODUCT_NOT_FOUND",
            "존재하지 않는 상품입니다."
    ),
    DUPLICATE_LOT_NUMBER(
            ErrorType.CONFLICT,
            "DUPLICATE_LOT_NUMBER",
            "해당 입고 건에 동일한 제조사 로트 번호가 이미 등록되어 있습니다."
    ),
    LOT_DATES_REQUIRED(
            ErrorType.INVALID_REQUEST,
            "LOT_DATES_REQUIRED",
            "제조일자와 유통기한은 필수 입력 값입니다."
    ),
    INVALID_MANUFACTURE_DATE(
            ErrorType.INVALID_REQUEST,
            "INVALID_MANUFACTURE_DATE",
            "제조일자는 유통기한보다 미래일 수 없습니다."
    ),
    INVALID_LOT_NUMBER_FORMAT(
            ErrorType.INVALID_REQUEST,
            "INVALID_LOT_NUMBER_FORMAT",
            "올바르지 않은 제조사 로트 번호 형식입니다. (영문 대문자·숫자·하이픈만 허용, 최대 20자)"
    ),
    LOT_PRODUCT_ID_REQUIRED(
            ErrorType.INVALID_REQUEST,
            "LOT_PRODUCT_ID_REQUIRED",
            "상품 식별자(ID)는 필수입니다."
    ),
    INVALID_LOT_STATUS_TRANSITION(
            ErrorType.RULE_VIOLATION,
            "INVALID_LOT_STATUS_TRANSITION",
            "허용되지 않은 로트 상태 전환입니다."
    );

    private final ErrorType type;
    private final String code;
    private final String message;
}