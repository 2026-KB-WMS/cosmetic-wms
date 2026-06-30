package com.kb.cosmetic_wms.storage.domain.model;

import com.kb.cosmetic_wms.storage.domain.exception.StorageErrorCode;
import com.kb.cosmetic_wms.storage.domain.exception.StorageValidationException;

import java.util.regex.Pattern;

public record SectionCode(String value) {

    private static final Pattern PATTERN =
            Pattern.compile("^WH\\d{2}-(DOCK|STR|QUAR)-[RC]-\\d{2}$");

    public SectionCode {
        if (value == null || value.isBlank()) {
            throw new StorageValidationException(StorageErrorCode.INVALID_SECTION_CODE);
        }
        value = value.trim();
        if (!PATTERN.matcher(value).matches()) {
            throw new StorageValidationException(StorageErrorCode.INVALID_SECTION_CODE);
        }
    }
}