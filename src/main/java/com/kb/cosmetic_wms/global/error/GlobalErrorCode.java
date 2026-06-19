package com.kb.cosmetic_wms.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    SECURITY_CONTEXT_NOT_FOUND(
            HttpStatus.UNAUTHORIZED,
            "SECURITY_CONTEXT_NOT_FOUND",
            "인증 컨텍스트를 찾을 수 없습니다. 로그인 후 다시 시도하세요."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
