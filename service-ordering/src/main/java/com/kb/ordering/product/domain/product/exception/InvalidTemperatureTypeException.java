package com.kb.ordering.product.domain.product.exception;

import com.kb.common.error.BusinessException;

public class InvalidTemperatureTypeException extends BusinessException {
    public InvalidTemperatureTypeException() {
        super(ProductErrorCode.INVALID_TEMPERATURE_TYPE);
    }
}