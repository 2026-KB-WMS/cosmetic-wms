package com.kb.cosmetic_wms.storage.adapter.out.persistence;

import com.kb.cosmetic_wms.storage.application.port.out.StoragePort;
import com.kb.cosmetic_wms.storage.domain.model.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StoragePersistenceAdapter implements StoragePort {

    private final WarehouseJpaRepository warehouseJpaRepository;

    @Override
    public boolean existsByNameAndAddress(String warehouseName, String address) {
        return warehouseJpaRepository.existsByWarehouseNameAndAddress(warehouseName, address);
    }

    @Override
    public Optional<Warehouse> findById(Long warehouseId) {
        return warehouseJpaRepository.findById(warehouseId)
                .map(WarehouseEntity::toDomain);
    }

    @Override
    public Optional<Warehouse> findByIdForUpdate(Long warehouseId) {
        return warehouseJpaRepository.findByIdForUpdate(warehouseId)
                .map(WarehouseEntity::toDomain);
    }

    @Override
    public List<Warehouse> findAll() {
        return warehouseJpaRepository.findAll().stream()
                .map(WarehouseEntity::toDomain)
                .toList();
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        WarehouseEntity entity = warehouseJpaRepository.save(WarehouseEntity.fromDomain(warehouse));
        return entity.toDomain();
    }
}
