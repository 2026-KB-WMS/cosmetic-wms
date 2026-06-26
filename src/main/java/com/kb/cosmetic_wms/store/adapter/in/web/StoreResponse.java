package com.kb.cosmetic_wms.store.adapter.in.web;

import com.kb.cosmetic_wms.store.application.port.in.StoreResult;

public record StoreResponse(
        Long id,
        String storeName,
        String address
) {
    public static StoreResponse from(StoreResult result) {
        return new StoreResponse(
                result.storeId(),
                result.storeName(),
                result.address()
        );
    }
}