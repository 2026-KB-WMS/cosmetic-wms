package com.kb.cosmetic_wms.member.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class MemberValidationException extends BusinessException {
    public MemberValidationException() {
        super(MemberErrorCode.INVALID_MEMBER_VALIDATION);
    }
}
