package com.kb.auth.auth.domain.exception;

import com.kb.common.error.BusinessException;

public class InvalidRefreshTokenException extends BusinessException {
    public InvalidRefreshTokenException() {
        super(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
}