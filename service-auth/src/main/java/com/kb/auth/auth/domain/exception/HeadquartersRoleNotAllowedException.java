package com.kb.auth.auth.domain.exception;

import com.kb.common.error.BusinessException;

public class HeadquartersRoleNotAllowedException extends BusinessException {

    public HeadquartersRoleNotAllowedException() {
        super(AuthErrorCode.HEADQUARTERS_ROLE_NOT_ALLOWED);
    }
}
