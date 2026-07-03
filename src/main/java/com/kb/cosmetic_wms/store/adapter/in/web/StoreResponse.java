package com.kb.cosmetic_wms.store.adapter.in.web;

import com.kb.cosmetic_wms.store.application.port.in.StoreResult;

import java.math.BigDecimal;

public record StoreResponse(
        Long id,
        String storeName,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static StoreResponse from(StoreResult result) {
        return new StoreResponse(
                result.storeId(),
                result.storeName(),
                result.address(),
                result.latitude(),
                result.longitude()
        );
    }
}
