package com.kb.cosmetic_wms.storage.application.service;

import com.kb.cosmetic_wms.storage.application.port.in.IncreaseSectionCapacityCommand;
import com.kb.cosmetic_wms.storage.application.port.in.IncreaseSectionCapacityUseCase;
import com.kb.cosmetic_wms.storage.application.port.in.ReduceDockingCapacityCommand;
import com.kb.cosmetic_wms.storage.application.port.in.ReduceDockingCapacityUseCase;
import com.kb.cosmetic_wms.storage.application.port.in.UpdateDockingCapacityCommand;
import com.kb.cosmetic_wms.storage.application.port.in.UpdateDockingCapacityUseCase;
import com.kb.cosmetic_wms.storage.application.port.out.ProductTemperatureQueryPort;
import com.kb.cosmetic_wms.storage.application.port.out.StoragePort;
import com.kb.cosmetic_wms.storage.domain.exception.WarehouseNotFoundException;
import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import com.kb.cosmetic_wms.storage.domain.model.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DockingCapacityService implements UpdateDockingCapacityUseCase, ReduceDockingCapacityUseCase, IncreaseSectionCapacityUseCase {

    private final StoragePort storagePort;
    private final ProductTemperatureQueryPort productTemperatureQueryPort;

    @Override
    public void updateDockingCapacity(UpdateDockingCapacityCommand command) {
        List<Long> productIds = command.lines().stream()
                .filter(l -> l.receivedQuantity() > 0)
                .map(UpdateDockingCapacityCommand.LineItem::productId)
                .distinct()
                .toList();

        if (productIds.isEmpty()) {
            return;
        }

        Map<Long, TemperatureZone> zoneByProductId =
                productTemperatureQueryPort.findTemperatureZonesByIds(productIds);

        Map<TemperatureZone, Integer> receivedByZone = command.lines().stream()
                .filter(l -> l.receivedQuantity() > 0)
                .collect(Collectors.groupingBy(
                        l -> zoneByProductId.get(l.productId()),
                        Collectors.summingInt(UpdateDockingCapacityCommand.LineItem::receivedQuantity)
                ));

        Warehouse warehouse = storagePort.findByIdForUpdate(command.warehouseId())
                .orElseThrow(WarehouseNotFoundException::new);
        warehouse.receiveToDocking(receivedByZone);
        storagePort.save(warehouse);
    }

    @Override
    public void reduceDockingCapacity(ReduceDockingCapacityCommand command) {
        List<Long> productIds = command.lines().stream()
                .filter(l -> l.quantity() > 0)
                .map(ReduceDockingCapacityCommand.LineItem::productId)
                .distinct()
                .toList();

        if (productIds.isEmpty()) {
            return;
        }

        Map<Long, TemperatureZone> zoneByProductId =
                productTemperatureQueryPort.findTemperatureZonesByIds(productIds);

        Map<TemperatureZone, Integer> releasedByZone = command.lines().stream()
                .filter(l -> l.quantity() > 0)
                .collect(Collectors.groupingBy(
                        l -> zoneByProductId.get(l.productId()),
                        Collectors.summingInt(ReduceDockingCapacityCommand.LineItem::quantity)
                ));

        Warehouse warehouse = storagePort.findByIdForUpdate(command.warehouseId())
                .orElseThrow(WarehouseNotFoundException::new);
        warehouse.releaseFromDocking(releasedByZone);
        storagePort.save(warehouse);
    }

    @Override
    public void increaseSectionCapacity(IncreaseSectionCapacityCommand command) {
        Warehouse warehouse = storagePort.findByIdForUpdate(command.warehouseId())
                .orElseThrow(WarehouseNotFoundException::new);
        warehouse.increaseToTargetSection(command.sectionId(), command.quantity());
        storagePort.save(warehouse);
    }
}
