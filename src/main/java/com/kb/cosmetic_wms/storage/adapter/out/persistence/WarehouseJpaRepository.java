package com.kb.cosmetic_wms.storage.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface WarehouseJpaRepository extends JpaRepository<WarehouseEntity, Long> {

    boolean existsByWarehouseNameAndAddress(String warehouseName, String address);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WarehouseEntity w WHERE w.id = :id")
    Optional<WarehouseEntity> findByIdForUpdate(@Param("id") Long id);
}