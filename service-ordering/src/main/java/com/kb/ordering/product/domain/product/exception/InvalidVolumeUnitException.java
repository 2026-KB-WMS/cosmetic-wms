package com.kb.ordering.product.domain.product.exception;

import com.kb.common.error.BusinessException;

public class InvalidVolumeUnitException extends BusinessException {
    public InvalidVolumeUnitException(String detailMessage) {
        super(ProductErrorCode.INVALID_VOLUME_UNIT, detailMessage);
    }
}