package com.kb.ordering.store.application.port.in.dto;

public record RegisterStoreCommand(
        String storeName,
        String address
) {
}