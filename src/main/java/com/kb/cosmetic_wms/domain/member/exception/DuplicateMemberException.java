package com.kb.cosmetic_wms.domain.member.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class DuplicateMemberException extends BusinessException {
    public DuplicateMemberException() {
        super(MemberErrorCode.DUPLICATE_LOGIN_ID);
    }
}
