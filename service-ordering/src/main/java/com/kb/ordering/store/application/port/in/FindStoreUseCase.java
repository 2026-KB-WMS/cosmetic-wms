package com.kb.ordering.store.application.port.in;

import com.kb.ordering.store.application.port.in.dto.StoreResult;

public interface FindStoreUseCase {
    StoreResult findById(Long storeId);
}