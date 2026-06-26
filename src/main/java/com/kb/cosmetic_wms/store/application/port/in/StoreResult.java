package com.kb.cosmetic_wms.store.application.port.in;

import com.kb.cosmetic_wms.store.domain.model.Store;

public record StoreResult(
        Long storeId,
        String storeName,
        String address
) {
    public static StoreResult from(Store store) {
        return new StoreResult(
                store.getStoreId(),
                store.getStoreName(),
                store.getAddress()
        );
    }
}