package com.kb.cosmetic_wms.store.domain.model;

import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;
import com.kb.cosmetic_wms.store.domain.exception.StoreValidationException;
import lombok.Getter;

@Getter
public class Store {

    private final Long storeId;
    private final String storeName;
    private final String address;
    private final GeoCoordinate coordinate;

    private Store(Long storeId, String storeName, String address, GeoCoordinate coordinate) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.address = address;
        this.coordinate = coordinate;
    }

    public static Store create(String storeName, String address, GeoCoordinate coordinate) {
        validateStoreName(storeName);
        validateAddress(address);
        validateCoordinate(coordinate);
        return new Store(null, storeName, address, coordinate);
    }

    public static Store reconstitute(Long storeId, String storeName, String address, GeoCoordinate coordinate) {
        return new Store(storeId, storeName, address, coordinate);
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

    private static void validateCoordinate(GeoCoordinate coordinate) {
        if (coordinate == null) {
            throw new StoreValidationException();
        }
    }
}
