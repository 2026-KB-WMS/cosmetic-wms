package com.kb.ordering.store.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface StoreJpaRepository extends JpaRepository<StoreEntity, Long> {
    boolean existsByStoreNameAndAddress(String storeName, String address);
}