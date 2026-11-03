package com.kb.cosmetic_wms.storage.adapter.out.external;

import com.kb.cosmetic_wms.product.product.application.port.in.FindProductUseCase;
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

    private final FindProductUseCase findProductUseCase;

    @Override
    public Map<Long, TemperatureZone> findTemperatureZonesByIds(Collection<Long> productIds) {
        return findProductUseCase.findTemperatureCodesByIds(productIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> TemperatureZone.fromShortCode(e.getValue())));
    }
}
