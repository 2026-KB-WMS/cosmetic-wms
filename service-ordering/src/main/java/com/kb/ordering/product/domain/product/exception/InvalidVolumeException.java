package com.kb.ordering.product.domain.product.exception;

import com.kb.common.error.BusinessException;

public class InvalidVolumeException extends BusinessException {
    public InvalidVolumeException(String detailMessage) {
        super(ProductErrorCode.INVALID_VOLUME, detailMessage);
    }
}