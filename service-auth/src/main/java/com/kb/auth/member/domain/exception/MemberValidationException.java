package com.kb.auth.member.domain.exception;

import com.kb.common.error.BusinessException;

public class MemberValidationException extends BusinessException {
    public MemberValidationException() {
        super(MemberErrorCode.INVALID_MEMBER_VALIDATION);
    }
}
