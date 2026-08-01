package com.kb.ordering.store.domain.exception;

import com.kb.common.error.BusinessException;

public class StoreNotFoundException extends BusinessException {
    public StoreNotFoundException() {
        super(StoreErrorCode.STORE_NOT_FOUND);
    }
}