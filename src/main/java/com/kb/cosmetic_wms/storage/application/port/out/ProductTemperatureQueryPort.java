package com.kb.cosmetic_wms.storage.application.port.out;

import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;

import java.util.Collection;
import java.util.Map;

public interface ProductTemperatureQueryPort {
    Map<Long, TemperatureZone> findTemperatureZonesByIds(Collection<Long> productIds);
}
