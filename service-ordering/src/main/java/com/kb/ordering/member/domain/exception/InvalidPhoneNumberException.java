package com.kb.ordering.member.domain.exception;


import com.kb.common.error.BusinessException;

public class InvalidPhoneNumberException extends BusinessException {
    public InvalidPhoneNumberException() {
        super(MemberErrorCode.INVALID_PHONE_NUMBER);
    }
}
