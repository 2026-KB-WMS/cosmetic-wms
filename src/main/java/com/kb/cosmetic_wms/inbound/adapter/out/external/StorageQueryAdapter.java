package com.kb.cosmetic_wms.inbound.adapter.out.external;

import com.kb.cosmetic_wms.inbound.application.port.out.StorageQueryPort;
import com.kb.cosmetic_wms.product.product.application.port.out.ProductPort;
import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.storage.application.port.out.StoragePort;
import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StorageQueryAdapter implements StorageQueryPort {

    private final StoragePort storagePort;
    private final ProductPort productPort;

    @Override
    public boolean existsById(Long warehouseId) {
        return storagePort.existsById(warehouseId);
    }

    @Override
    public boolean canAccommodate(Long warehouseId, List<IncomingProduct> items) {
        List<Long> productIds = items.stream().map(IncomingProduct::productId).toList();
        Map<Long, TemperatureType> temperatureByProductId = productPort.findTemperatureTypesByIds(productIds);

        Map<TemperatureZone, Integer> requiredByZone = items.stream()
                .collect(Collectors.groupingBy(
                        item -> toTemperatureZone(temperatureByProductId.get(item.productId())),
                        Collectors.summingInt(IncomingProduct::quantity)
                ));

        return storagePort.canAccommodate(warehouseId, requiredByZone);
    }

    private TemperatureZone toTemperatureZone(TemperatureType productTemperatureType) {
        return switch (productTemperatureType) {
            case ROOM -> TemperatureZone.ROOM;
            case COOL -> TemperatureZone.COOL;
        };
    }
}