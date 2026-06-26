package com.kb.cosmetic_wms.product.product.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidTemperatureTypeException extends BusinessException {
    public InvalidTemperatureTypeException() {
        super(ProductErrorCode.INVALID_TEMPERATURE_TYPE);
    }
}