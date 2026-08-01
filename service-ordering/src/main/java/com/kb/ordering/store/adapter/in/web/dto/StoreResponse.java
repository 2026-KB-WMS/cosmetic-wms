package com.kb.ordering.store.adapter.in.web.dto;

import com.kb.ordering.store.application.port.in.dto.StoreResult;

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
