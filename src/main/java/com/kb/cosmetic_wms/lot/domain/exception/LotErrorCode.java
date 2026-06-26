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
    ),
    LOT_DATES_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "LOT_DATES_REQUIRED",
            "제조일자와 유통기한은 필수 입력 값입니다."
    ),
    INVALID_MANUFACTURE_DATE(
            HttpStatus.BAD_REQUEST,
            "INVALID_MANUFACTURE_DATE",
            "제조일자는 유통기한보다 미래일 수 없습니다."
    ),
    LOT_NUMBER_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "LOT_NUMBER_REQUIRED",
            "로트 번호는 필수 입력 값입니다."
    ),
    INVALID_LOT_NUMBER_FORMAT(
            HttpStatus.BAD_REQUEST,
            "INVALID_LOT_NUMBER_FORMAT",
            "올바르지 않은 로트 번호 형식입니다. (규격: [카테고리3자]-[YYMMDD]-[공장2자]-[일련번호4자])"
    ),
    LOT_PRODUCT_ID_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "LOT_PRODUCT_ID_REQUIRED",
            "상품 식별자(ID)는 필수입니다."
    ),
    INVALID_LOT_STATUS_TRANSITION(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "INVALID_LOT_STATUS_TRANSITION",
            "허용되지 않은 로트 상태 전환입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}