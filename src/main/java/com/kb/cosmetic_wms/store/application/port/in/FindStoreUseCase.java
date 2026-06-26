package com.kb.cosmetic_wms.store.application.port.in;

public interface FindStoreUseCase {
    StoreResult findById(Long storeId);
}