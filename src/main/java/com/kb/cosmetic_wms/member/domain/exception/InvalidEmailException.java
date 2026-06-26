package com.kb.cosmetic_wms.member.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidEmailException extends BusinessException {
    public InvalidEmailException() {
        super(MemberErrorCode.INVALID_EMAIL);
    }
}
