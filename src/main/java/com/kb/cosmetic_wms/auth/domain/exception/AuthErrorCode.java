package com.kb.cosmetic_wms.auth.domain.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import com.kb.cosmetic_wms.global.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_REFRESH_TOKEN(
            ErrorType.UNAUTHORIZED,
            "INVALID_REFRESH_TOKEN",
            "유효하지 않거나 만료된 리프레시 토큰입니다."
    );

    private final ErrorType type;
    private final String code;
    private final String message;
}
