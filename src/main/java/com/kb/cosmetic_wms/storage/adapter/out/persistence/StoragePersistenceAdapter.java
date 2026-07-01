package com.kb.cosmetic_wms.storage.adapter.out.persistence;

import com.kb.cosmetic_wms.storage.application.port.out.StoragePort;
import com.kb.cosmetic_wms.storage.domain.exception.WarehouseNotFoundException;
import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import com.kb.cosmetic_wms.storage.domain.model.Warehouse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StoragePersistenceAdapter implements StoragePort {

    private final WarehouseJpaRepository warehouseJpaRepository;
    private final EntityManager entityManager;

    @Override
    public boolean existsByNameAndAddress(String warehouseName, String address) {
        return warehouseJpaRepository.existsByWarehouseNameAndAddress(warehouseName, address);
    }

    @Override
    public boolean existsById(Long warehouseId) {
        return warehouseJpaRepository.existsById(warehouseId);
    }

    @Override
    public Optional<Warehouse> findById(Long warehouseId) {
        return warehouseJpaRepository.findById(warehouseId)
                .map(WarehouseEntity::toDomain);
    }

    @Override
    public Optional<Warehouse> findByIdForUpdate(Long warehouseId) {
        return warehouseJpaRepository.findByIdForUpdate(warehouseId)
                .map(entity -> {
                    // canAccommodate()의 findById()가 같은 트랜잭션 내 1차 캐시에 stale entity를 올려두므로,
                    // JPQL의 @Lock(PESSIMISTIC_WRITE)가 DB lock은 걸지만 반환값은 캐시의 stale entity가 된다.
                    // refresh로 lock 획득 상태에서 DB 최신값을 강제로 1차 캐시에 반영한다.
                    entityManager.refresh(entity);
                    return entity.toDomain();
                });
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

    @Override
    public boolean canAccommodate(Long warehouseId, Map<TemperatureZone, Integer> requiredByZone) {
        Warehouse warehouse = warehouseJpaRepository.findById(warehouseId)
                .map(WarehouseEntity::toDomain)
                .orElseThrow(WarehouseNotFoundException::new);
        return warehouse.canAccommodateDocking(requiredByZone);
    }
}
