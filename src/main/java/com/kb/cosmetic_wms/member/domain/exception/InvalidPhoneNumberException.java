package com.kb.cosmetic_wms.member.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidPhoneNumberException extends BusinessException {
    public InvalidPhoneNumberException() {
        super(MemberErrorCode.INVALID_PHONE_NUMBER);
    }
}
