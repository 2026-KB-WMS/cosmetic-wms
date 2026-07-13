package com.kb.ordering.member.domain.valueobject;

import com.kb.ordering.member.domain.exception.InvalidPhoneNumberException;

import java.util.regex.Pattern;

public record PhoneNumber(String value) {

    private static final String REGEX =
            "^(01[016789]|02|0[3-9][0-9])-(?:\\d{3}|\\d{4})-\\d{4}$";

    public PhoneNumber {
        if (value == null || !Pattern.matches(REGEX, value)) {
            throw new InvalidPhoneNumberException();
        }
    }
}