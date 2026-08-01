package com.kb.ordering.store.domain.exception;

import com.kb.common.error.ErrorCode;
import com.kb.common.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreErrorCode implements ErrorCode {
    STORE_NOT_FOUND(
            ErrorType.NOT_FOUND,
            "STORE_NOT_FOUND",
            "존재하지 않는 가맹점입니다."
    ),

    DUPLICATE_STORE(
            ErrorType.INVALID_REQUEST,
            "DUPLICATE_STORE",
            "이미 동일한 주소에 등록된 가맹점 점포가 존재합니다."
    ),

    STORE_VALIDATION_FAILED(
            ErrorType.INVALID_REQUEST,
            "STORE_VALIDATION_FAILED",
            "가맹점 정보 유효성 검증에 실패했습니다."
    );

    private final ErrorType type;
    private final String code;
    private final String message;
}