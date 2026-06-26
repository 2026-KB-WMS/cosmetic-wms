package com.kb.cosmetic_wms.member.domain.valueobject;

import com.kb.cosmetic_wms.member.domain.exception.InvalidEmailException;

import java.util.regex.Pattern;

public record Email(String value) {

    private static final String REGEX =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    public Email {
        if (value == null || !Pattern.matches(REGEX, value)) {
            throw new InvalidEmailException();
        }
    }
}