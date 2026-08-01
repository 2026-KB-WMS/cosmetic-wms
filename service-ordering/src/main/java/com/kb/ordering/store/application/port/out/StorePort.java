package com.kb.ordering.store.application.port.out;

import com.kb.ordering.store.domain.model.Store;

import java.util.Optional;

public interface StorePort {
    boolean existsByStoreNameAndAddress(String storeName, String address);

    Optional<Store> findById(Long storeId);

    Store save(Store store);
}