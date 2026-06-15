package com.kb.cosmetic_wms.domain.member.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class MemberNotFoundException extends BusinessException {
    public MemberNotFoundException() {
        super(MemberErrorCode.MEMBER_NOT_FOUND);
    }
}
