package com.kb.cosmetic_wms.domain.storage.repository;

import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    boolean existsByWarehouseNameAndAddress(String warehouseName, String address);
}
