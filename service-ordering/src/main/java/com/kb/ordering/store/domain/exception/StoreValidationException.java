package com.kb.ordering.store.domain.exception;

import com.kb.common.error.BusinessException;

public class StoreValidationException extends BusinessException {
    public StoreValidationException() {
        super(StoreErrorCode.STORE_VALIDATION_FAILED);
    }
}