package com.kb.auth.auth.domain.valueobject;

import com.kb.auth.auth.domain.exception.CredentialValidationException;

public record LoginId(String value) {

    public static final int MIN_LENGTH = 5;
    public static final int MAX_LENGTH = 50;

    public LoginId {
        if (value == null || value.isBlank()) {
            throw new CredentialValidationException();
        }
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new CredentialValidationException();
        }
    }
}
