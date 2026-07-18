package com.kb.ordering.product.domain.product.valueobject;

import com.kb.ordering.product.domain.product.exception.InvalidPriceException;

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