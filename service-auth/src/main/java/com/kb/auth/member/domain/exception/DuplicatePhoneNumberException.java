package com.kb.auth.member.domain.exception;

import com.kb.common.error.BusinessException;

public class DuplicatePhoneNumberException extends BusinessException {

    public DuplicatePhoneNumberException() {
        super(MemberErrorCode.DUPLICATE_PHONE_NUMBER);
    }
}
