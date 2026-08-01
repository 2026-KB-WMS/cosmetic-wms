package com.kb.ordering.store.application.port.in.dto;

import com.kb.ordering.store.domain.model.Store;

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
