package com.kb.cosmetic_wms.product.product.domain.valueobject;

import com.kb.cosmetic_wms.product.product.domain.exception.InvalidPriceException;

public record Price(int value) {

    public static final int MIN_VALUE = 0;

    public Price {
        if (value < MIN_VALUE) {
            throw new InvalidPriceException();
        }
    }

    public static Price of(int value) {
        return new Price(value);
    }
}