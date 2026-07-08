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
    );

    private final ErrorType type;
    private final String code;
    private final String message;
}
