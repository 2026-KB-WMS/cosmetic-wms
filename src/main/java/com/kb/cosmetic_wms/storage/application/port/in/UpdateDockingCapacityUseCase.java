package com.kb.cosmetic_wms.storage.application.port.in;

import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;

import java.util.Map;

public interface UpdateDockingCapacityUseCase {
    void updateDockingCapacity(Long warehouseId, Map<TemperatureZone, Integer> receivedByZone);
}
