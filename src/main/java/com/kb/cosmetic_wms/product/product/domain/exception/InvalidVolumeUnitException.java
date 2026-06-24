package com.kb.cosmetic_wms.product.product.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidVolumeUnitException extends BusinessException {
    public InvalidVolumeUnitException(String detailMessage) {
        super(ProductErrorCode.INVALID_VOLUME_UNIT, detailMessage);
    }
}