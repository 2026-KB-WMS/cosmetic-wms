package com.kb.ordering.auth.domain.exception;

import com.kb.common.error.BusinessException;

public class DuplicateLoginIdException extends BusinessException {
    public DuplicateLoginIdException() {
        super(AuthErrorCode.DUPLICATE_LOGIN_ID);
    }
}
