package com.kb.cosmetic_wms.storage.application.port.out;

import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import com.kb.cosmetic_wms.storage.domain.model.Warehouse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface StoragePort {

    boolean existsByNameAndAddress(String warehouseName, String address);

    Optional<Warehouse> findById(Long warehouseId);

    Optional<Warehouse> findByIdForUpdate(Long warehouseId);

    List<Warehouse> findAll();

    Warehouse save(Warehouse warehouse);

    boolean existsById(Long warehouseId);

    boolean canAccommodate(Long warehouseId, Map<TemperatureZone, Integer> requiredByZone);
}