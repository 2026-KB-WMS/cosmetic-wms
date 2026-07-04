package com.kb.cosmetic_wms.storage.application.service;

import com.kb.cosmetic_wms.storage.application.port.in.ApplyInspectionCapacityCommand;
import com.kb.cosmetic_wms.storage.application.port.in.ApplyInspectionCapacityUseCase;
import com.kb.cosmetic_wms.storage.application.port.in.CheckWarehouseCapacityCommand;
import com.kb.cosmetic_wms.storage.application.port.in.CheckWarehouseCapacityUseCase;
import com.kb.cosmetic_wms.storage.application.port.in.SectionAssignmentResult;
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
public class DockingCapacityService implements UpdateDockingCapacityUseCase, ApplyInspectionCapacityUseCase,
        CheckWarehouseCapacityUseCase {

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
    @Transactional(readOnly = true)
    public boolean canAccommodate(CheckWarehouseCapacityCommand command) {
        List<Long> productIds = command.items().stream()
                .map(CheckWarehouseCapacityCommand.ProductQuantity::productId)
                .toList();
        Map<Long, TemperatureZone> zoneByProductId =
                productTemperatureQueryPort.findTemperatureZonesByIds(productIds);

        Map<TemperatureZone, Integer> requiredByZone = command.items().stream()
                .collect(Collectors.groupingBy(
                        item -> zoneByProductId.get(item.productId()),
                        Collectors.summingInt(CheckWarehouseCapacityCommand.ProductQuantity::quantity)
                ));

        return storagePort.canAccommodate(command.warehouseId(), requiredByZone);
    }

    @Override
    public SectionAssignmentResult applyInspectionCapacity(ApplyInspectionCapacityCommand command) {
        int totalQty = command.passedQuantity() + command.failedQuantity();
        if (totalQty <= 0) {
            return new SectionAssignmentResult(null, null);
        }

        TemperatureZone zone = productTemperatureQueryPort
                .findTemperatureZonesByIds(List.of(command.productId()))
                .get(command.productId());

        Warehouse warehouse = storagePort.findByIdForUpdate(command.warehouseId())
                .orElseThrow(WarehouseNotFoundException::new);

        warehouse.releaseFromDocking(Map.of(zone, totalQty));

        Long storageSectionId = null;
        Long quarantineSectionId = null;

        if (command.passedQuantity() > 0) {
            storageSectionId = warehouse.selectStorageSectionAndIncrease(zone, command.passedQuantity());
        }
        if (command.failedQuantity() > 0) {
            quarantineSectionId = warehouse.selectQuarantineSectionAndIncrease(command.failedQuantity());
        }

        storagePort.save(warehouse);
        return new SectionAssignmentResult(storageSectionId, quarantineSectionId);
    }
}
