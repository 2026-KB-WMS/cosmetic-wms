package com.kb.cosmetic_wms.store.domain.model;

import com.kb.cosmetic_wms.store.domain.exception.StoreValidationException;
import lombok.Getter;

@Getter
public class Store {

    private final Long storeId;
    private final String storeName;
    private final String address;

    private Store(Long storeId, String storeName, String address) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.address = address;
    }

    public static Store create(String storeName, String address) {
        validateStoreName(storeName);
        validateAddress(address);
        return new Store(null, storeName, address);
    }

    public static Store reconstitute(Long storeId, String storeName, String address) {
        return new Store(storeId, storeName, address);
    }

    private static void validateStoreName(String storeName) {
        if (storeName == null || storeName.isBlank()) {
            throw new StoreValidationException();
        }
    }

    private static void validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new StoreValidationException();
        }
    }
}