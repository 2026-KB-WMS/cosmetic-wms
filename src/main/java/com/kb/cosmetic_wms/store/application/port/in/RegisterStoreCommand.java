package com.kb.cosmetic_wms.store.application.port.in;

public record RegisterStoreCommand(
        String storeName,
        String address
) {
}