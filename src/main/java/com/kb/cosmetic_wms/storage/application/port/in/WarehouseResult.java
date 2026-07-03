package com.kb.cosmetic_wms.storage.application.port.in;

import com.kb.cosmetic_wms.storage.domain.model.Warehouse;

import java.math.BigDecimal;
import java.util.List;

public record WarehouseResult(
        Long warehouseId,
        String warehouseName,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String targetTemp,
        int capacity,
        List<SectionResult> sections
) {
    public static WarehouseResult from(Warehouse warehouse) {
        return new WarehouseResult(
                warehouse.getWarehouseId(),
                warehouse.getWarehouseName(),
                warehouse.getAddress(),
                warehouse.getCoordinate().latitude(),
                warehouse.getCoordinate().longitude(),
                warehouse.getTargetTempValue(),
                warehouse.getCapacity(),
                warehouse.getSections().stream().map(SectionResult::from).toList()
        );
    }
}
