package com.kb.auth.auth.domain.exception;

import com.kb.common.error.BusinessException;

public class CredentialValidationException extends BusinessException {
    public CredentialValidationException() {
        super(AuthErrorCode.INVALID_CREDENTIAL);
    }
}
