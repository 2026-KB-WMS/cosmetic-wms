package com.kb.cosmetic_wms.domain.member.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class LoginFailedException extends BusinessException {
    public LoginFailedException() {
        super(MemberErrorCode.LOGIN_FAILED);
    }
}
