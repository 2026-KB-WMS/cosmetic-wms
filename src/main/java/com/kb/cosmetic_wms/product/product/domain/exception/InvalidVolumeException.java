package com.kb.cosmetic_wms.product.product.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidVolumeException extends BusinessException {
    public InvalidVolumeException(String detailMessage) {
        super(ProductErrorCode.INVALID_VOLUME, detailMessage);
    }
}