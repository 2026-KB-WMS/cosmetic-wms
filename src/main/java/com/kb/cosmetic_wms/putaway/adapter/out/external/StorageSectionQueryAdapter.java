package com.kb.cosmetic_wms.putaway.adapter.out.external;

import com.kb.cosmetic_wms.product.product.application.port.out.ProductPort;
import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.putaway.application.port.out.StorageSectionQueryPort;
import com.kb.cosmetic_wms.putaway.domain.exception.PutawayTargetSectionNotFoundException;
import com.kb.cosmetic_wms.storage.application.port.out.StoragePort;
import com.kb.cosmetic_wms.storage.domain.model.Section;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;
import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import com.kb.cosmetic_wms.storage.domain.model.Warehouse;
import com.kb.cosmetic_wms.storage.domain.exception.WarehouseNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StorageSectionQueryAdapter implements StorageSectionQueryPort {

    private final StoragePort storagePort;
    private final ProductPort productPort;

    @Override
    public Long findDockingSectionId(Long warehouseId, Long productId) {
        Warehouse warehouse = loadWarehouse(warehouseId);
        TemperatureZone zone = resolveTemperatureZone(productId);
        return findSections(warehouse, SectionType.DOCKING, zone).stream()
                .findFirst()
                .map(Section::getSectionId)
                .orElseThrow(PutawayTargetSectionNotFoundException::new);
    }

    @Override
    public Optional<Long> findAvailableStorageSectionId(Long warehouseId, Long productId) {
        Warehouse warehouse = loadWarehouse(warehouseId);
        TemperatureZone zone = resolveTemperatureZone(productId);
        return findSections(warehouse, SectionType.STORAGE, zone).stream()
                .filter(s -> s.getCurrentCapacity() < s.getMaxCapacity())
                .findFirst()
                .map(Section::getSectionId);
    }

    @Override
    public Optional<Long> findAvailableQuarantineSectionId(Long warehouseId) {
        Warehouse warehouse = loadWarehouse(warehouseId);
        return warehouse.getSections().stream()
                .filter(s -> s.getSectionType() == SectionType.QUARANTINE)
                .filter(s -> s.getCurrentCapacity() < s.getMaxCapacity())
                .findFirst()
                .map(Section::getSectionId);
    }

    private Warehouse loadWarehouse(Long warehouseId) {
        return storagePort.findById(warehouseId)
                .orElseThrow(WarehouseNotFoundException::new);
    }

    private TemperatureZone resolveTemperatureZone(Long productId) {
        TemperatureType type = productPort.findTemperatureTypesByIds(List.of(productId))
                .get(productId);
        return type == TemperatureType.COOL ? TemperatureZone.COOL : TemperatureZone.ROOM;
    }

    private List<Section> findSections(Warehouse warehouse, SectionType type, TemperatureZone zone) {
        return warehouse.getSections().stream()
                .filter(s -> s.getSectionType() == type && s.getTemperatureType() == zone)
                .toList();
    }
}
