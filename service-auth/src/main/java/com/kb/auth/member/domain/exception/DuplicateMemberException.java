package com.kb.auth.member.domain.exception;


import com.kb.common.error.BusinessException;

public class DuplicateMemberException extends BusinessException {
    public DuplicateMemberException() {
        super(MemberErrorCode.DUPLICATE_EMAIL);
    }
}
