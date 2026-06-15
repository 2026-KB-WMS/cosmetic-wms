package com.kb.cosmetic_wms.domain.store.repository;

import com.kb.cosmetic_wms.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

    boolean existsByStoreNameAndAddress(String storeName, String address);
}
