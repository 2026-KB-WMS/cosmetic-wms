package com.kb.ordering.product.domain.shared;

import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class CodeValidator {

    public static final int REQUIRED_LENGTH = 3;
    private static final Pattern UPPERCASE_ONLY = Pattern.compile("^[A-Z]+$");

    private CodeValidator() {}

    public static void validate(String value,
                                Supplier<RuntimeException> onBlank,
                                Supplier<RuntimeException> onWrongLength,
                                Supplier<RuntimeException> onInvalidChars) {
        if (value == null || value.isBlank()) throw onBlank.get();
        if (value.length() != REQUIRED_LENGTH) throw onWrongLength.get();
        if (!UPPERCASE_ONLY.matcher(value).matches()) throw onInvalidChars.get();
    }
}
