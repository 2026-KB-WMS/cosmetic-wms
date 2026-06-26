package com.kb.cosmetic_wms.store.adapter.out.persistence;

import com.kb.cosmetic_wms.store.application.port.out.StorePort;
import com.kb.cosmetic_wms.store.domain.model.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StorePersistenceAdapter implements StorePort {

    private final StoreJpaRepository storeJpaRepository;

    @Override
    public boolean existsByStoreNameAndAddress(String storeName, String address) {
        return storeJpaRepository.existsByStoreNameAndAddress(storeName, address);
    }

    @Override
    public Optional<Store> findById(Long storeId) {
        return storeJpaRepository.findById(storeId)
                .map(StoreEntity::toDomain);
    }

    @Override
    public Store save(Store store) {
        return storeJpaRepository.save(StoreEntity.fromDomain(store)).toDomain();
    }
}