package com.kb.cosmetic_wms.member.domain.valueobject;

import com.kb.cosmetic_wms.member.domain.exception.InvalidPasswordException;

import java.util.regex.Pattern;

public record RawPassword(String value) {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 50;
    public static final String PATTERN =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$";

    private static final Pattern COMPILED_PATTERN = Pattern.compile(PATTERN);

    public RawPassword {
        if (value == null
                || value.length() < MIN_LENGTH
                || value.length() > MAX_LENGTH
                || !COMPILED_PATTERN.matcher(value).matches()) {
            throw new InvalidPasswordException();
        }
    }

    @Override
    public String toString() {
        return "RawPassword[****]";
    }
}
