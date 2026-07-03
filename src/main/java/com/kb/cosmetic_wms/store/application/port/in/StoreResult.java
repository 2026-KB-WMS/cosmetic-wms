package com.kb.cosmetic_wms.store.application.port.in;

import com.kb.cosmetic_wms.store.domain.model.Store;

import java.math.BigDecimal;

public record StoreResult(
        Long storeId,
        String storeName,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static StoreResult from(Store store) {
        return new StoreResult(
                store.getStoreId(),
                store.getStoreName(),
                store.getAddress(),
                store.getCoordinate().latitude(),
                store.getCoordinate().longitude()
        );
    }
}
