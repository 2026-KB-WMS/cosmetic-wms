package com.kb.cosmetic_wms.domain.member.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class MemberValidationException extends BusinessException {
    public MemberValidationException() {
        super(MemberErrorCode.INVALID_MEMBER_VALIDATION);
    }
}
