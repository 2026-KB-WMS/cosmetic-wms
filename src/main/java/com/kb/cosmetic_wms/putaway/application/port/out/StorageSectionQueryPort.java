package com.kb.cosmetic_wms.putaway.application.port.out;

import java.util.Optional;

public interface StorageSectionQueryPort {
    Long findDockingSectionId(Long warehouseId, Long productId);
    Optional<Long> findAvailableStorageSectionId(Long warehouseId, Long productId);
    Optional<Long> findAvailableQuarantineSectionId(Long warehouseId);
}
