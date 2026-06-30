package com.kb.cosmetic_wms.putaway.domain.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PutawayErrorCode implements ErrorCode {

    PUTAWAY_ORDER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PUTAWAY_ORDER_NOT_FOUND",
            "존재하지 않는 적재 작업 지시서입니다."
    ),
    TARGET_SECTION_NOT_FOUND(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "PUTAWAY_TARGET_SECTION_NOT_FOUND",
            "적재 가능한 목적지 구역이 없습니다."
    ),
    ALREADY_COMPLETED(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "PUTAWAY_ALREADY_COMPLETED",
            "이미 완료된 적재 작업 지시서입니다."
    ),
    INVALID_QUANTITY(
            HttpStatus.BAD_REQUEST,
            "PUTAWAY_INVALID_QUANTITY",
            "적재 수량은 0보다 커야 합니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
