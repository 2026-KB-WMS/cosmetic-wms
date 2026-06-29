package com.kb.cosmetic_wms.inventory.adapter.out.external;

import com.kb.cosmetic_wms.inventory.application.port.out.DockingSectionQueryPort;
import com.kb.cosmetic_wms.product.product.application.port.out.ProductPort;
import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.storage.application.port.out.StoragePort;
import com.kb.cosmetic_wms.storage.domain.model.Section;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;
import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import com.kb.cosmetic_wms.storage.domain.model.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DockingSectionQueryAdapter implements DockingSectionQueryPort {

    private final StoragePort storagePort;
    private final ProductPort productPort;

    @Override
    public Long findDockingSectionId(Long warehouseId, Long productId) {
        Map<Long, TemperatureType> tempTypes = productPort.findTemperatureTypesByIds(java.util.List.of(productId));
        TemperatureZone zone = toZone(tempTypes.get(productId));

        Warehouse warehouse = storagePort.findById(warehouseId)
                .orElseThrow(() -> new IllegalStateException("창고를 찾을 수 없습니다: " + warehouseId));

        return warehouse.getSections().stream()
                .filter(s -> s.getSectionType() == SectionType.DOCKING && s.getTemperatureType() == zone)
                .findFirst()
                .map(Section::getSectionId)
                .orElseThrow(() -> new IllegalStateException(
                        "적합한 DOCKING 섹션을 찾을 수 없습니다. warehouseId=" + warehouseId + ", zone=" + zone));
    }

    private TemperatureZone toZone(TemperatureType type) {
        return switch (type) {
            case ROOM -> TemperatureZone.ROOM;
            case COOL -> TemperatureZone.COOL;
        };
    }
}
