package com.kb.cosmetic_wms.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    SECURITY_CONTEXT_NOT_FOUND(
            ErrorType.UNAUTHORIZED,
            "SECURITY_CONTEXT_NOT_FOUND",
            "인증 컨텍스트를 찾을 수 없습니다. 로그인 후 다시 시도하세요."
    ),
    UNAUTHENTICATED(
            ErrorType.UNAUTHORIZED,
            "UNAUTHENTICATED",
            "인증이 필요합니다. 로그인 후 다시 시도하세요."
    ),
    INVALID_TOKEN(
            ErrorType.UNAUTHORIZED,
            "INVALID_TOKEN",
            "유효하지 않거나 만료된 토큰입니다. 토큰을 재발급받으세요."
    ),
    ACCESS_DENIED(
            ErrorType.FORBIDDEN,
            "ACCESS_DENIED",
            "해당 요청에 대한 권한이 없습니다."
    );

    private final ErrorType type;
    private final String code;
    private final String message;
}
