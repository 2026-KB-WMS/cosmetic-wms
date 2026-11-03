package com.kb.cosmetic_wms.auth.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidRefreshTokenException extends BusinessException {
    public InvalidRefreshTokenException() {
        super(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
}
