package com.kb.auth.auth.domain.exception;

import com.kb.common.error.BusinessException;

public class LoginFailedException extends BusinessException {
    public LoginFailedException() {
        super(AuthErrorCode.LOGIN_FAILED);
    }
}
