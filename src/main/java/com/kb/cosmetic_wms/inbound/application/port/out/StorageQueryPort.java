package com.kb.cosmetic_wms.inbound.application.port.out;

public interface StorageQueryPort {
    boolean existsById(Long warehouseId);
}
