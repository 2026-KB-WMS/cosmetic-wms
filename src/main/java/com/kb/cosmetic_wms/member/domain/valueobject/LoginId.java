package com.kb.cosmetic_wms.member.domain.valueobject;

import com.kb.cosmetic_wms.member.domain.exception.MemberValidationException;

public record LoginId(String value) {

    public static final int MIN_LENGTH = 5;
    public static final int MAX_LENGTH = 50;

    public LoginId {
        if (value == null || value.isBlank()) {
            throw new MemberValidationException();
        }
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new MemberValidationException();
        }
    }
}