package com.kb.cosmetic_wms.lot.domain.model;

import com.kb.cosmetic_wms.lot.domain.exception.InvalidLotNumberFormatException;
import com.kb.cosmetic_wms.lot.domain.exception.LotNumberRequiredException;

import java.util.regex.Pattern;

// [상품 카테고리 3자리]-[제조년월일 6자리]-[공장코드 2자리]-[일련번호 4자리]
public record LotNumber(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[A-Z]{3}-\\d{6}-[A-Z0-9]{2}-\\d{4}$");

    public LotNumber {
        if (value == null || value.isBlank()) {
            throw new LotNumberRequiredException();
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new InvalidLotNumberFormatException();
        }
    }
}