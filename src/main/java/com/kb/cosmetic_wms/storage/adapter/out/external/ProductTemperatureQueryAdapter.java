package com.kb.cosmetic_wms.storage.adapter.out.external;

import com.kb.cosmetic_wms.product.product.application.port.out.ProductPort;
import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.storage.application.port.out.ProductTemperatureQueryPort;
import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductTemperatureQueryAdapter implements ProductTemperatureQueryPort {

    private final ProductPort productPort;

    @Override
    public Map<Long, TemperatureZone> findTemperatureZonesByIds(Collection<Long> productIds) {
        return productPort.findTemperatureTypesByIds(productIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> toZone(e.getValue())));
    }

    private TemperatureZone toZone(TemperatureType type) {
        return switch (type) {
            case ROOM -> TemperatureZone.ROOM;
            case COOL -> TemperatureZone.COOL;
        };
    }
}
