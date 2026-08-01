package com.kb.auth.member.domain.exception;


import com.kb.common.error.BusinessException;

public class InvalidEmailException extends BusinessException {
    public InvalidEmailException() {
        super(MemberErrorCode.INVALID_EMAIL);
    }
}
