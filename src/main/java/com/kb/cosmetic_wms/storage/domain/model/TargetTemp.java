package com.kb.cosmetic_wms.storage.domain.model;

import com.kb.cosmetic_wms.storage.domain.exception.InvalidTargetTempException;

import java.util.regex.Pattern;

public record TargetTemp(String value) {

    private static final Pattern REGEX = Pattern.compile("^\\d+~\\d+도$");

    public TargetTemp {
        if (value == null || value.isBlank() || !REGEX.matcher(value.trim()).matches()) {
            throw new InvalidTargetTempException();
        }
    }
}
