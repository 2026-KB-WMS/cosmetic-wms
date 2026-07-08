package com.kb.cosmetic_wms.storage.application.port.in;

import java.util.List;

public interface FindWarehouseUseCase {

    WarehouseResult findById(Long warehouseId);

    List<WarehouseResult> findAll();

    boolean existsById(Long warehouseId);
}
