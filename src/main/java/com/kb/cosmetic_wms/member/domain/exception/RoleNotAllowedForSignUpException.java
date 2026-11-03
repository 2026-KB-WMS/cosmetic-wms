package com.kb.cosmetic_wms.member.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class RoleNotAllowedForSignUpException extends BusinessException {
    public RoleNotAllowedForSignUpException() {
        super(MemberErrorCode.ROLE_NOT_ALLOWED_FOR_SIGN_UP);
    }
}
