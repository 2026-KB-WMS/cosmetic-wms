package com.kb.cosmetic_wms.global.error;

/**
 * 도메인 오류의 분류. HTTP 상태 코드로의 변환은 web 계층(GlobalExceptionHandler)이 담당한다.
 */
public enum ErrorType {
    INVALID_REQUEST,
    UNAUTHORIZED,
    NOT_FOUND,
    CONFLICT,
    RULE_VIOLATION,
    INTERNAL_ERROR,
    EXTERNAL_SERVICE_ERROR
}
