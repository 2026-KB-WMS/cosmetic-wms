package com.kb.cosmetic_wms.storage.application.service;

import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;
import com.kb.cosmetic_wms.global.geocoding.GeocodingPort;
import com.kb.cosmetic_wms.storage.application.port.in.*;
import com.kb.cosmetic_wms.storage.application.port.out.StoragePort;
import com.kb.cosmetic_wms.storage.domain.exception.DuplicateWarehouseException;
import com.kb.cosmetic_wms.storage.domain.exception.WarehouseNotFoundException;
import com.kb.cosmetic_wms.storage.domain.model.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StorageService implements RegisterWarehouseUseCase, AddSectionUseCase, FindWarehouseUseCase {

    private final StoragePort storagePort;
    private final GeocodingPort geocodingPort;

    @Override
    @Transactional
    public WarehouseResult register(RegisterWarehouseCommand command) {
        if (storagePort.existsByNameAndAddress(command.warehouseName(), command.address())) {
            throw new DuplicateWarehouseException();
        }
        GeoCoordinate coordinate = geocodingPort.geocode(command.address());
        Warehouse warehouse = Warehouse.create(
                command.warehouseName(),
                command.address(),
                coordinate,
                command.targetTemp(),
                command.capacity()
        );
        return WarehouseResult.from(storagePort.save(warehouse));
    }

    @Override
    @Transactional
    public WarehouseResult addSection(Long warehouseId, AddSectionCommand command) {
        Warehouse warehouse = storagePort.findByIdForUpdate(warehouseId)
                .orElseThrow(WarehouseNotFoundException::new);
        warehouse.addSection(command.sectionType(), command.sectionName(),
                command.temperatureType(), command.maxCapacity());
        return WarehouseResult.from(storagePort.save(warehouse));
    }

    @Override
    public WarehouseResult findById(Long warehouseId) {
        return storagePort.findById(warehouseId)
                .map(WarehouseResult::from)
                .orElseThrow(WarehouseNotFoundException::new);
    }

    @Override
    public List<WarehouseResult> findAll() {
        return storagePort.findAll().stream()
                .map(WarehouseResult::from)
                .toList();
    }
}
