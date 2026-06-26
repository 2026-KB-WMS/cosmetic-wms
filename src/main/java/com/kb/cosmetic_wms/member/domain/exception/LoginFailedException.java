package com.kb.cosmetic_wms.member.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class LoginFailedException extends BusinessException {
    public LoginFailedException() {
        super(MemberErrorCode.LOGIN_FAILED);
    }
}
