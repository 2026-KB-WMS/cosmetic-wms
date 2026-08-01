package com.kb.ordering.store.domain.exception;

import com.kb.common.error.BusinessException;

public class DuplicateStoreException extends BusinessException {
    public DuplicateStoreException() {
        super(StoreErrorCode.DUPLICATE_STORE);
    }
}