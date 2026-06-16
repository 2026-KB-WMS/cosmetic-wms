package com.kb.cosmetic_wms.domain.store.dto;

import com.kb.cosmetic_wms.domain.store.entity.Store;

public record StoreResponseDto(
        Long id,
        String storeName,
        String address
) {
    public static StoreResponseDto from(Store store) {
        return new StoreResponseDto(
                store.getId(),
                store.getStoreName(),
                store.getAddress()
        );
    }
}
